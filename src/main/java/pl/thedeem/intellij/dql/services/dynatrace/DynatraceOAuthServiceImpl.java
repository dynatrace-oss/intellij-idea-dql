package pl.thedeem.intellij.dql.services.dynatrace;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.DefinitionUtils;
import pl.thedeem.intellij.common.sdk.SimpleOAuthClient;
import pl.thedeem.intellij.common.sdk.errors.SSOAuthException;
import pl.thedeem.intellij.common.sdk.errors.SSONotConfiguredException;
import pl.thedeem.intellij.common.sdk.errors.SSOReAuthRequiredException;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.services.dynatrace.model.OAuthConfig;
import pl.thedeem.intellij.dql.services.dynatrace.model.OAuthEnvironment;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DynatraceOAuthServiceImpl implements DynatraceOAuthService {
    private static final String DEFINITION_FILE = "oauth/config.json";
    private static final String CRED_PREFIX = "oauth-refresh-";
    private final Object[] stripedLocks = new Object[16];
    private final Map<String, Map<String, Object>> tokenCache = new ConcurrentHashMap<>();
    private final SimpleOAuthClient client;

    private volatile OAuthConfig config;

    public DynatraceOAuthServiceImpl() {
        this.client = new SimpleOAuthClient();
        for (int i = 0; i < stripedLocks.length; i++) {
            stripedLocks[i] = new Object();
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> signIn(@NotNull String environmentUrl, @NotNull String credentialId) {
        OAuthEnvironment oauth = getOAuthEnvironment(environmentUrl);
        return client.startAuthFlow(oauth.authUrl(), oauth.tokenUrl(), oauth.clientId(), environmentUrl, getConfig().defaultScopes(), BrowserUtil::browse)
                .thenAccept(tokens -> persistSecret(credentialId, tokens, null));
    }

    @Override
    public @Nullable String resolveToken(@NotNull String credentialId, @NotNull String environmentUrl) throws SSOAuthException, InterruptedException {
        synchronized (stripedLocks[Math.floorMod(credentialId.hashCode(), stripedLocks.length)]) {
            Map<String, Object> cached = tokenCache.get(credentialId);
            if (cached != null && Instant.now().plusSeconds(60).isBefore(Instant.ofEpochMilli((long) cached.get("valid_to")))) {
                return (String) cached.get("access_token");
            }

            String storedRefreshToken = loadRefreshToken(credentialId);
            if (storedRefreshToken == null) {
                throw new SSONotConfiguredException("Not logged into Dynatrace SSO");
            }

            try {
                OAuthEnvironment oauth = getOAuthEnvironment(environmentUrl);
                Map<String, Object> refreshed = client.refreshToken(oauth.tokenUrl(), oauth.clientId(), storedRefreshToken);
                persistSecret(credentialId, refreshed, storedRefreshToken);

                return (String) refreshed.get("access_token");
            } catch (SSOReAuthRequiredException e) {
                signOut(credentialId);
                throw e;
            }
        }
    }

    @Override
    public void signOut(@NotNull String credentialId) {
        persistSecret(credentialId, null, null);
    }

    private void persistSecret(@NotNull String id, @Nullable Map<String, Object> refreshed, @Nullable String currentToken) {
        String tokenToStore = refreshed != null ? (String) refreshed.getOrDefault("refresh_token", currentToken) : currentToken;
        if (refreshed != null) {
            tokenCache.put(id, refreshed);
        } else {
            tokenCache.remove(id);
        }
        CredentialAttributes attributes = DQLUtil.createCredentialAttributes(CRED_PREFIX + id);
        Credentials credentials = tokenToStore != null ? new Credentials("oauth", tokenToStore) : null;
        PasswordSafe.getInstance().set(attributes, credentials);
    }

    private @Nullable String loadRefreshToken(String id) {
        return PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(CRED_PREFIX + id));
    }

    private @NotNull OAuthConfig getConfig() {
        if (this.config == null) {
            synchronized (this) {
                if (this.config == null) {
                    config = loadConfiguration();
                }
            }
        }
        return this.config;
    }

    private @NotNull OAuthEnvironment getOAuthEnvironment(@NotNull String environmentUrl) {
        OAuthConfig currentConfig = getConfig();
        String hostname = getHostnameFromUrl(environmentUrl);
        for (OAuthEnvironment environment : currentConfig.environments()) {
            List<String> domains = Objects.requireNonNullElseGet(environment.domains(), List::of);
            if (domains.stream().anyMatch(domain -> StringUtil.endsWithIgnoreCase(hostname, domain))) {
                return environment;
            }
        }
        return currentConfig.environments().isEmpty() ? OAuthEnvironment.empty() : currentConfig.environments().getLast();
    }

    private @NotNull String getHostnameFromUrl(@NotNull String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException ignored) {
            return url;
        }
    }

    private OAuthConfig loadConfiguration() {
        OAuthConfig loaded = DefinitionUtils.loadDefinitionFromFile(DEFINITION_FILE, OAuthConfig.class);
        return Objects.requireNonNullElse(loaded, OAuthConfig.empty());
    }
}
package pl.thedeem.intellij.dql.services.dynatrace;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.sdk.errors.SSOAuthException;

import java.util.concurrent.CompletableFuture;

public interface DynatraceOAuthService {
    static DynatraceOAuthService getInstance() {
        return ApplicationManager.getApplication().getService(DynatraceOAuthService.class);
    }

    @NotNull CompletableFuture<Void> signIn(@NotNull String environmentUrl, @NotNull String credentialId);

    @Nullable String resolveToken(@NotNull String credentialId, @NotNull String environmentUrl) throws SSOAuthException, InterruptedException;

    void signOut(@NotNull String credentialId);
}
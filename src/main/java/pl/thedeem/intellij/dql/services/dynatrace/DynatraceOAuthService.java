package pl.thedeem.intellij.dql.services.dynatrace;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface DynatraceOAuthService {
    static DynatraceOAuthService getInstance() {
        return ApplicationManager.getApplication().getService(DynatraceOAuthService.class);
    }

    @NotNull CompletableFuture<Void> signIn(@NotNull String environmentUrl, @NotNull String credentialId);

    @NotNull CompletableFuture<@Nullable String> resolveToken(@NotNull String credentialId, @NotNull String environmentUrl);

    @NotNull CompletableFuture<Void> signOut(@NotNull String credentialId);
}


package pl.thedeem.intellij.dql.services.dynatrace;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.sdk.model.DQLAutocompleteResult;
import pl.thedeem.intellij.common.sdk.model.DQLExecutePayload;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.common.sdk.model.DQLVerifyResponse;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface DynatraceRestService {
    static @NotNull DynatraceRestService getInstance(@NotNull Project project) {
        return project.getService(DynatraceRestService.class);
    }

    @NotNull CompletableFuture<DQLVerifyResponse> verifyQuery(@NotNull DynatraceTenant tenant, @NotNull String query);

    @NotNull CompletableFuture<DQLAutocompleteResult> autocompleteQuery(@NotNull DynatraceTenant tenant, @NotNull String query, long offset);

    @NotNull CompletableFuture<DQLPollResponse> executeQuery(
            @NotNull DynatraceTenant tenant,
            @NotNull DQLExecutePayload payload,
            @Nullable Consumer<DQLPollResponse> progressConsumer
    );

    @NotNull <T> CompletableFuture<T> withStandardErrorHandling(@NotNull CompletableFuture<T> future, @NotNull DynatraceTenant tenant);
}

package pl.thedeem.intellij.dql.services.ui;

import com.intellij.execution.services.ServiceViewDescriptor;
import com.intellij.openapi.actionSystem.DataKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DQLManagedService<T> extends ServiceViewDescriptor {
    DataKey<DQLManagedService<?>> EXECUTION_SERVICE = DataKey.create("executionService");

    void startExecution(@NotNull T params);

    void stopExecution();

    boolean isRunning();

    @NotNull String getName();

    @Nullable String getExecutionId();
}

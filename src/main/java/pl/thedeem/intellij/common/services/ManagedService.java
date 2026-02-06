package pl.thedeem.intellij.common.services;

import com.intellij.execution.services.ServiceViewDescriptor;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ManagedService extends ServiceViewDescriptor, Disposable {
    @NotNull String getServiceId();

    @NotNull List<ManagedServiceGroup> getParentGroups();
}

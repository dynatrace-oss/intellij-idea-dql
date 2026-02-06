package pl.thedeem.intellij.common.services;

import com.intellij.execution.services.ServiceViewDescriptor;
import com.intellij.execution.services.ServiceViewGroupingContributor;
import com.intellij.execution.services.SimpleServiceViewDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import pl.thedeem.intellij.common.Icons;

import java.util.List;

public class ManagedServiceViewContributor implements ServiceViewGroupingContributor<ManagedService, ManagedServiceGroup> {
    @Override
    public @NotNull ServiceViewDescriptor getViewDescriptor(@NotNull Project project) {
        return new SimpleServiceViewDescriptor("Dynatrace Query Language Services", Icons.DYNATRACE_LOGO);
    }

    @Override
    public @NotNull @Unmodifiable List<ManagedService> getServices(@NotNull Project project) {
        return ProjectServicesManager.getInstance(project).getRegisteredServices();
    }

    @Override
    public @NotNull ServiceViewDescriptor getServiceDescriptor(@NotNull Project project, @NotNull ManagedService service) {
        return service;
    }

    @Override
    public @NotNull List<ManagedServiceGroup> getGroups(@NotNull ManagedService managedService) {
        return managedService.getParentGroups();
    }

    @Override
    public @NotNull ServiceViewDescriptor getGroupDescriptor(@NotNull ManagedServiceGroup group) {
        return group;
    }
}

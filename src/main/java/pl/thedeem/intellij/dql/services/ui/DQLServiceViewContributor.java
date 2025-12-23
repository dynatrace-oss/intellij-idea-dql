package pl.thedeem.intellij.dql.services.ui;

import com.intellij.execution.services.ServiceViewContributor;
import com.intellij.execution.services.ServiceViewDescriptor;
import com.intellij.execution.services.SimpleServiceViewDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import pl.thedeem.intellij.dql.DQLIcon;

import java.util.List;

public class DQLServiceViewContributor implements ServiceViewContributor<DQLManagedService<?>> {
    @Override
    public @NotNull ServiceViewDescriptor getViewDescriptor(@NotNull Project project) {
        return new SimpleServiceViewDescriptor("DQL", DQLIcon.DYNATRACE_LOGO);
    }

    @Override
    public @NotNull @Unmodifiable List<DQLManagedService<?>> getServices(@NotNull Project project) {
        return DQLServicesManager.getInstance(project).getActiveServices();
    }

    @Override
    public @NotNull ServiceViewDescriptor getServiceDescriptor(@NotNull Project project, @NotNull DQLManagedService service) {
        return service;
    }
}

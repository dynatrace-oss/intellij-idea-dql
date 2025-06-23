package pl.thedeem.intellij.dql.executing.services;

import com.intellij.execution.services.ServiceViewContributor;
import com.intellij.execution.services.ServiceViewDescriptor;
import com.intellij.execution.services.SimpleServiceViewDescriptor;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.executing.executeDql.DQLExecutionService;

import javax.swing.*;
import java.util.List;

public class DQLServiceViewContributor implements ServiceViewContributor<DQLExecutionService> {
    @Override
    public @NotNull ServiceViewDescriptor getViewDescriptor(@NotNull Project project) {
        return new SimpleServiceViewDescriptor("DQL", DQLIcon.DYNATRACE_LOGO);
    }

    @Override
    public @NotNull @Unmodifiable List<DQLExecutionService> getServices(@NotNull Project project) {
        return DQLServicesManager.getInstance(project).getActiveServices();
    }

    @Override
    public @NotNull ServiceViewDescriptor getServiceDescriptor(@NotNull Project project, @NotNull DQLExecutionService service) {
        return new ServiceViewDescriptor() {
            @Override
            public @NotNull ItemPresentation getPresentation() {
                return service.getPresentation();
            }

            @Override
            public @Nullable ActionGroup getToolbarActions() {
                return service.getToolbarActions();
            }

            @Override
            public @Nullable JComponent getContentComponent() {
                return service.getContentComponent();
            }
        };
    }
}

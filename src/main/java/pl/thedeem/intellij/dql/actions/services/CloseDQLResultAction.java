package pl.thedeem.intellij.dql.actions.services;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;
import pl.thedeem.intellij.dql.services.ui.DQLServicesManager;

public class CloseDQLResultAction extends AbstractServiceAction {
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull DQLExecutionService service, @NotNull Project project) {
        DQLServicesManager.getInstance(project).stopExecution(service);
    }
}

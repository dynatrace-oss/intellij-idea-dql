package pl.thedeem.intellij.dql.actions.services;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.actions.ActionUtils;
import pl.thedeem.intellij.dql.services.ui.DQLManagedService;
import pl.thedeem.intellij.dql.services.ui.DQLServicesManager;

public class CloseDQLResultAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DQLManagedService<?> service = ActionUtils.getService(e);
        Project project = e.getProject();
        if (project == null || service == null) {
            return;
        }
        DQLServicesManager.getInstance(project).stopExecution(service);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DQLManagedService<?> service = ActionUtils.getService(e);
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        if (project == null || service == null) {
            presentation.setEnabledAndVisible(false);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}

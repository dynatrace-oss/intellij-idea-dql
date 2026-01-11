package pl.thedeem.intellij.dql.actions.services;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.actions.ActionUtils;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;

public abstract class AbstractServiceAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        Project project = e.getProject();
        if (project == null || service == null) {
            return;
        }
        actionPerformed(e, service, project);
    }

    protected abstract void actionPerformed(@NotNull AnActionEvent e, @NotNull DQLExecutionService service, @NotNull Project project);

    @Override
    public void update(@NotNull AnActionEvent e) {
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        if (project == null || service == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        update(e, service, project, presentation);
    }

    protected void update(@NotNull AnActionEvent e, @NotNull DQLExecutionService service, @NotNull Project project, @NotNull Presentation presentation) {
        // To be overridden
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}

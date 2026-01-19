package pl.thedeem.intellij.dql.exec.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.actions.ActionUtils;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;

public class DQLResultsAsJsonAction extends ToggleAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        if (service == null || service.isRunning()) {
            e.getPresentation().setEnabledAndVisible(false);
        }
        super.update(e);
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        return service != null && service.getDisplayMode() == DQLExecutionService.ResultsDisplayMode.JSON;
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean b) {
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        if (service != null && service.getDisplayMode() != DQLExecutionService.ResultsDisplayMode.JSON) {
            service.setDisplayMode(DQLExecutionService.ResultsDisplayMode.JSON);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}

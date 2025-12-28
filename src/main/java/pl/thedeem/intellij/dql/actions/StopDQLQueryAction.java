package pl.thedeem.intellij.dql.actions;

import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;
import pl.thedeem.intellij.dql.services.ui.DQLManagedService;
import pl.thedeem.intellij.dql.services.ui.DQLServicesManager;

import java.util.List;
import java.util.Objects;

public class StopDQLQueryAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        Presentation presentation = e.getPresentation();
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        if (project == null || (service == null && ActionUtils.isNotRelatedToDQL(e))) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        List<DQLManagedService<?>> services = runningServices(project, file, service);
        presentation.setEnabled(!services.isEmpty());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        e.getPresentation().setEnabled(false);
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        for (DQLManagedService<?> related : runningServices(project, file, service)) {
            related.stopExecution();
        }
        ActivityTracker.getInstance().inc();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private List<DQLManagedService<?>> runningServices(@NotNull Project project, @Nullable PsiFile file, @Nullable DQLExecutionService service) {
        String serviceName = service != null ? service.getName() : file != null ? ActionUtils.generateServiceName(file) : null;
        return DQLServicesManager.getInstance(project).findServices(ex -> ex instanceof DQLExecutionService && ex.isRunning() && Objects.equals(ex.getName(), serviceName));
    }
}

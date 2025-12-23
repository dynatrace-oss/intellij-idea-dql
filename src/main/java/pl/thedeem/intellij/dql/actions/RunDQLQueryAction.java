package pl.thedeem.intellij.dql.actions;

import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;
import pl.thedeem.intellij.dql.executing.DQLProcessHandler;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.ui.DQLServicesManager;

public class RunDQLQueryAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        if (project == null || (service == null && ActionUtils.isNotRelatedToDQL(e))) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        presentation.setEnabled(service == null || !service.isRunning());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance(project);
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        // for the service execution context, we need to execute the exact same query that was passed
        if (service != null && service.getConfiguration() != null) {
            e.getPresentation().setEnabled(false);
            DQLServicesManager.getInstance(project).startExecution(new DQLExecutionService(service.getName(), project, new DQLProcessHandler()), service.getConfiguration());
            return;
        }

        Editor editor = ActionUtils.getEditor(e);
        PsiFile file = ActionUtils.getRelatedPsiFile(e.getData(CommonDataKeys.PSI_FILE), editor, e.getData(CommonDataKeys.PSI_ELEMENT));
        if (file == null) {
            return;
        }
        configurationService.getQueryConfigurationWithEditorContext(file, editor, (configuration) -> {
            e.getPresentation().setEnabled(false);
            String serviceName = ActionUtils.generateServiceName(file);
            DQLServicesManager.getInstance(project).startExecution(new DQLExecutionService(serviceName, project, new DQLProcessHandler()), configuration);
            ActivityTracker.getInstance().inc();
        });
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

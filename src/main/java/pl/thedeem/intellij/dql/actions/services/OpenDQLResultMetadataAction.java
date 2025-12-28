package pl.thedeem.intellij.dql.actions.services;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.actions.ActionUtils;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;
import pl.thedeem.intellij.dql.fileProviders.DQLMetadataVirtualFile;

public class OpenDQLResultMetadataAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        Project project = e.getProject();
        if (project == null || service == null) {
            return;
        }
        DQLResult result = service.getResult();
        if (result == null) {
            return;
        }

        FileEditorManager.getInstance(project).openFile(new DQLMetadataVirtualFile(
                        DQLBundle.message("components.queryDetails.fileName", service.getName()),
                        service.getResult().getGrailMetadata(),
                        service.getExecutionTime()
                ), true
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        if (project == null || service == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        presentation.setEnabledAndVisible(service.getResult() != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}

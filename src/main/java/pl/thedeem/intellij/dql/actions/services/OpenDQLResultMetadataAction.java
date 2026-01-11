package pl.thedeem.intellij.dql.actions.services;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;
import pl.thedeem.intellij.dql.fileProviders.DQLMetadataVirtualFile;

public class OpenDQLResultMetadataAction extends AbstractServiceAction {
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull DQLExecutionService service, @NotNull Project project) {
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
    protected void update(@NotNull AnActionEvent e, @NotNull DQLExecutionService service, @NotNull Project project, @NotNull Presentation presentation) {
        presentation.setEnabledAndVisible(service.getResult() != null);
    }
}

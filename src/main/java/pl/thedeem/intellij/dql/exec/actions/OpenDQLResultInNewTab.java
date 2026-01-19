package pl.thedeem.intellij.dql.exec.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;
import pl.thedeem.intellij.dql.fileProviders.DQLResultVirtualFile;

public class OpenDQLResultInNewTab extends AbstractServiceAction {
    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull DQLExecutionService service, @NotNull Project project) {
        DQLPollResponse result = service.getResult();
        if (result == null) {
            return;
        }

        FileEditorManager.getInstance(project)
                .openFile(new DQLResultVirtualFile(
                        DQLBundle.message("components.queryDetails.fileName", service.getName()),
                        result
                ), true);
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull DQLExecutionService service, @NotNull Project project, @NotNull Presentation presentation) {
        presentation.setEnabledAndVisible(service.getResult() != null);
    }
}

package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.exec.panel.DQLExecutionResult;

import javax.swing.*;

public class DQLResultVirtualFile extends DQLVirtualFile<DQLPollResponse> {
    public DQLResultVirtualFile(@NotNull String name, @NotNull DQLPollResponse content) {
        super(name, content);
    }

    @Override
    public @NotNull JComponent createComponent(@NotNull Project project) {
        DQLExecutionResult panel = new DQLExecutionResult(project);
        panel.update(content);
        return panel;
    }
}

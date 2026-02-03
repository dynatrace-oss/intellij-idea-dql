package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
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
        BorderLayoutPanel panel = JBUI.Panels.simplePanel();
        panel.setBorder(JBUI.Borders.empty());
        DQLExecutionResult result = new DQLExecutionResult(project);
        result.update(content);
        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DQL.ResultToolbar", result.getToolbarActions(), true);
        toolbar.setTargetComponent(panel);
        panel.addToTop(toolbar.getComponent());
        panel.addToCenter(result);
        return panel;
    }
}

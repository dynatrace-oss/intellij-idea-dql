package pl.thedeem.intellij.dql.components.editor;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

public class DQLToolbarProvider implements EditorNotificationProvider {
    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (!DQLFileType.INSTANCE.equals(virtualFile.getFileType()) || !DQLSettings.getInstance().isDQLExecutionToolbarVisible()) {
            return null;
        }

        ActionManager actionManager = ActionManager.getInstance();
        return fileEditor -> {
            JPanel container = createToolbarPanel();

            ActionToolbar toolbar = createActionToolbar(actionManager);
            toolbar.setTargetComponent(container);
            container.add(toolbar.getComponent(), BorderLayout.WEST);

            ActionToolbar closeToolbar = createManagerToolbar(actionManager);
            closeToolbar.setTargetComponent(container);
            closeToolbar.getComponent().setOpaque(false);
            container.add(closeToolbar.getComponent(), BorderLayout.EAST);

            return container;
        };
    }

    private static @NotNull JPanel createToolbarPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, JBColor.border()));
        container.setOpaque(false);
        return container;
    }

    private static @NotNull ActionToolbar createActionToolbar(ActionManager actionManager) {
        ActionToolbar toolbar = actionManager.createActionToolbar("DQLFloatingToolbarActions", (ActionGroup) actionManager.getAction("DQL.FloatingToolbar"), true);
        toolbar.getComponent().setOpaque(false);
        return toolbar;
    }

    private static @NotNull ActionToolbar createManagerToolbar(ActionManager actionManager) {
        ActionToolbar toolbar = actionManager.createActionToolbar("DQLFloatingToolbarManagerActions", (ActionGroup) actionManager.getAction("DQL.FloatingToolbarManager"), true);
        toolbar.getComponent().setOpaque(false);
        return toolbar;
    }
}

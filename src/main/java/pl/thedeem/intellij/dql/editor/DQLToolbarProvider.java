package pl.thedeem.intellij.dql.editor;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.editor.actions.ExecutionManagerAction;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import javax.swing.*;
import java.util.function.Function;

public class DQLToolbarProvider implements EditorNotificationProvider {
    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(
            @NotNull Project project, @NotNull VirtualFile virtualFile
    ) {
        if (!DQLFileType.INSTANCE.equals(virtualFile.getFileType()) || !DQLSettings.getInstance().isDQLExecutionToolbarVisible()) {
            return null;
        }

        PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
        if (file == null) {
            return null;
        }
        ActionManager actionManager = ActionManager.getInstance();
        return fileEditor -> {
            BorderLayoutPanel container = JBUI.Panels.simplePanel();
            ActionToolbar toolbar = createToolbarPanel(actionManager, file);
            toolbar.setTargetComponent(container);
            container.addToLeft(toolbar.getComponent());
            ActionToolbar closeToolbar = createManagerToolbar(actionManager);
            closeToolbar.setTargetComponent(container);
            JComponent toolbarComponent = closeToolbar.getComponent();
            toolbarComponent.setBorder(JBUI.Borders.empty());
            toolbarComponent.setOpaque(false);
            container.addToRight(toolbarComponent);
            container.setOpaque(false);
            container.setBorder(JBUI.Borders.empty());
            return new TransparentScrollPane(container);
        };
    }

    private static @NotNull ActionToolbar createToolbarPanel(@NotNull ActionManager actionManager, @NotNull PsiFile file) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.setInjectedContext(true);
        group.addAction(new ExecutionManagerAction(file));
        ActionToolbar toolbar = actionManager.createActionToolbar("DQL.FloatingToolbar", group, true);
        toolbar.getComponent().setOpaque(false);
        toolbar.getComponent().setBorder(JBUI.Borders.empty());
        return toolbar;
    }


    private static @NotNull ActionToolbar createManagerToolbar(@NotNull ActionManager actionManager) {
        ActionToolbar toolbar = actionManager.createActionToolbar("DQL.FloatingToolbarManagerActions", (ActionGroup) actionManager.getAction("DQL.FloatingToolbarManager"), true);
        toolbar.getComponent().setOpaque(false);
        toolbar.getComponent().setBorder(JBUI.Borders.empty());
        return toolbar;
    }
}

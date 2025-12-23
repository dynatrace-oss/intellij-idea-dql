package pl.thedeem.intellij.dql.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.settings.DQLSettings;

public class ToggleDQLExecutionToolbarAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        Presentation presentation = e.getPresentation();
        if (file == null || !DQLFileType.INSTANCE.equals(file.getFileType())) {
            presentation.setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DQLSettings.getInstance().setDQLExecutionToolbarVisible(!DQLSettings.getInstance().isDQLExecutionToolbarVisible());
        if (e.getProject() != null) {
            EditorNotifications.getInstance(e.getProject()).updateAllNotifications();
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

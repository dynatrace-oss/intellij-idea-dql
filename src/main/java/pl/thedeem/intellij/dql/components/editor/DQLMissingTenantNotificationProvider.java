package pl.thedeem.intellij.dql.components.editor;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.settings.DQLSettingsConfigurable;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import javax.swing.*;
import java.util.function.Function;

public class DQLMissingTenantNotificationProvider implements EditorNotificationProvider {
    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        if (!DQLFileType.INSTANCE.equals(virtualFile.getFileType())) {
            return null;
        }
        DynatraceTenantsService tenantsService = DynatraceTenantsService.getInstance();
        if (!shouldShowToolbar(tenantsService)) {
            return null;
        }
        return (FileEditor fileEditor) -> {
            EditorNotificationPanel panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info);
            panel.setText(DQLBundle.message("notifications.noDynatraceTenants.info"));

            panel.createActionLabel(DQLBundle.message("notifications.noDynatraceTenants.actions.add"), () -> {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, DQLSettingsConfigurable.class);
                panel.setVisible(shouldShowToolbar(tenantsService));
                EditorNotifications.getInstance(project).updateNotifications(virtualFile);
            });
            panel.setCloseAction(() -> {
                tenantsService.dismissMissingTenantsNotification();
                EditorNotifications.getInstance(project).updateNotifications(virtualFile);
            });
            return panel;
        };
    }

    private boolean shouldShowToolbar(@NotNull DynatraceTenantsService tenantsService) {
        return tenantsService.getTenants().isEmpty() && !tenantsService.isMissingTenantsNotificationDismissed();
    }
}

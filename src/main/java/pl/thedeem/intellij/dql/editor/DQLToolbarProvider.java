package pl.thedeem.intellij.dql.editor;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.SimpleDataProviderPanel;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.editor.actions.QueryConfigurationAction;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import javax.swing.*;
import java.util.function.Function;

public class DQLToolbarProvider implements EditorNotificationProvider {
    public static final Key<Boolean> TOOLBAR_HIDDEN = Key.create("DQL.TOOLBAR_SHOWN");

    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(
            @NotNull Project project,
            @NotNull VirtualFile virtualFile
    ) {
        if (!DQLFileType.INSTANCE.equals(virtualFile.getFileType())
                || !DQLSettings.getInstance().isDQLExecutionToolbarVisible()
                || Boolean.TRUE.equals(virtualFile.getUserData(TOOLBAR_HIDDEN))) {
            return null;
        }

        PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
        if (file == null) {
            return null;
        }
        ActionManager actionManager = ActionManager.getInstance();
        return fileEditor -> {
            BorderLayoutPanel container = new SimpleDataProviderPanel() {
                @Override
                public void uiDataSnapshot(@NotNull DataSink dataSink) {
                    DQLQueryConfigurationService service = DQLQueryConfigurationService.getInstance();
                    dataSink.lazy(DQLQueryConfigurationService.DATA_TENANT, () -> service.getQueryConfiguration(file).tenant());
                    dataSink.lazy(DQLQueryConfigurationService.DATA_TIMEFRAME_START, () -> service.getQueryConfiguration(file).timeframeStart());
                    dataSink.lazy(DQLQueryConfigurationService.DATA_TIMEFRAME_END, () -> service.getQueryConfiguration(file).timeframeEnd());
                    dataSink.lazy(DQLQueryConfigurationService.DATA_DEFAULT_SCAN_LIMIT, () -> service.getQueryConfiguration(file).defaultScanLimit());
                    dataSink.lazy(DQLQueryConfigurationService.DATA_MAX_RESULT_BYTES, () -> service.getQueryConfiguration(file).maxResultBytes());
                    dataSink.lazy(DQLQueryConfigurationService.DATA_MAX_RESULT_RECORDS, () -> service.getQueryConfiguration(file).maxResultRecords());
                    dataSink.lazy(DQLQueryConfigurationService.DATA_ORIGINAL_FILE, () -> service.getQueryConfiguration(file).originalFile());
                    dataSink.lazy(DQLQueryConfigurationService.DATA_RUN_CONFIG_NAME, () -> service.getQueryConfiguration(file).runConfigName());
                }
            };
            ActionToolbar toolbar = createToolbarPanel(actionManager);
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

    private static @NotNull ActionToolbar createToolbarPanel(@NotNull ActionManager actionManager) {
        DefaultActionGroup group = new DefaultActionGroup();
        group.setInjectedContext(true);
        group.addAction(new QueryConfigurationAction());
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

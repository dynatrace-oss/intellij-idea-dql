package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import javax.swing.*;
import java.util.concurrent.Callable;

public class ExecutionManagerAction extends AnAction implements CustomComponentAction {
    private final DQLExecutionManagerToolbar myComponent;

    public ExecutionManagerAction(@NotNull PsiFile file) {
        super(DQLBundle.message("action.DQLExecutionManagerToolbar.text"), null, AllIcons.Actions.Execute);
        this.myComponent = new DQLExecutionManagerToolbar();
        myComponent.init(() -> {
            DQLQueryConfigurationService configService = DQLQueryConfigurationService.getInstance();
            return configService.getQueryConfiguration(file);
        }, file);
    }

    public ExecutionManagerAction(@NotNull DQLExecutionService service) {
        this.myComponent = new DQLExecutionManagerToolbar();
        myComponent.init(service::getConfiguration, null);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (e.isFromContextMenu()) {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        return myComponent;
    }

    private static final class DQLExecutionManagerToolbar extends BorderLayoutPanel implements UiDataProvider {
        private static final DataKey<Boolean> SHOW_MORE_OPTIONS = DataKey.create("DQL_SHOW_MORE_OPTIONS");
        private QueryConfiguration configuration;
        private PsiFile file;
        private boolean showMoreOptions = false;

        public DQLExecutionManagerToolbar() {
            setBorder(JBUI.Borders.empty());
            setOpaque(false);
        }

        public void init(@NotNull Callable<QueryConfiguration> configurationCallback, @Nullable PsiFile file) {
            this.file = file;
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    this.configuration = configurationCallback.call();
                    ApplicationManager.getApplication().invokeLater(this::refreshUi);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        @Override
        public void uiDataSnapshot(@NotNull DataSink dataSink) {
            dataSink.set(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION, configuration);
            dataSink.set(SHOW_MORE_OPTIONS, showMoreOptions);
            if (file != null) {
                file.putUserData(DQLQueryConfigurationService.QUERY_CONFIGURATION, configuration);
            }
        }

        private void refreshUi() {
            DefaultActionGroup group = new DefaultActionGroup();
            ActionManager actionManager = ActionManager.getInstance();
            group.add(actionManager.getAction("DQL.SelectTenant"));
            group.add(actionManager.getAction("DQL.StartStopExecution"));
            group.addSeparator();
            if (file != null) {
                addQueryConfiguration(group);
                group.addSeparator();
                group.add(actionManager.getAction("DQL.SaveQueryConfiguration"));
            }
            ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DQL.ExecutionManagerAction", group, true);
            toolbar.setTargetComponent(this);
            JComponent toolbarComponent = toolbar.getComponent();
            toolbarComponent.setOpaque(false);
            toolbarComponent.setBorder(JBUI.Borders.empty());
            addToTop(toolbarComponent);
        }

        private void addQueryConfiguration(@NotNull DefaultActionGroup group) {
            group.add(new AbstractTimeFieldAction(
                    configuration.timeframeStart(),
                    DQLBundle.message("action.DQL.QueryConfigurationAction.timeframeFrom.placeholder"),
                    DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe"),
                    AllIcons.General.History
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
                    if (configuration == null) {
                        return;
                    }
                    configuration.setTimeframeStart(getValue());
                }
            });
            group.add(new AbstractTimeFieldAction(
                    configuration.timeframeEnd(),
                    DQLBundle.message("action.DQL.QueryConfigurationAction.timeframeTo.placeholder"),
                    DQLBundle.message("action.DQLExecutionManagerToolbar.option.queryTimeframe"),
                    null
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
                    if (configuration == null) {
                        return;
                    }
                    configuration.setTimeframeEnd(getValue());
                }
            });

            group.addAction(new ToggleAction(
                    DQLBundle.message("action.DQLExecutionManagerToolbar.moreOptions"),
                    null,
                    AllIcons.Actions.ToggleVisibility
            ) {
                @Override
                public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
                    return showMoreOptions;
                }

                @Override
                public void setSelected(@NotNull AnActionEvent anActionEvent, boolean selected) {
                    showMoreOptions = selected;
                    ActivityTracker.getInstance().inc();
                }

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }
            });

            group.addAction(new AbstractNumericFieldAction(
                    configuration.defaultScanLimit(),
                    DQLBundle.message("action.DQL.QueryConfigurationAction.scanLimit.placeholder"),
                    DQLBundle.message("action.DQLExecutionManagerToolbar.option.scanLimit"),
                    AllIcons.Actions.GroupByModule
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
                    if (configuration == null) {
                        return;
                    }
                    configuration.setDefaultScanLimit(getValue());
                }

                @Override
                protected boolean isVisible(@NotNull AnActionEvent e) {
                    return Boolean.TRUE.equals(e.getData(SHOW_MORE_OPTIONS));
                }
            });
            group.addAction(new AbstractNumericFieldAction(
                    configuration.maxResultBytes(),
                    DQLBundle.message("action.DQL.QueryConfigurationAction.maxBytes.placeholder"),
                    DQLBundle.message("action.DQLExecutionManagerToolbar.option.maxBytes"),
                    AllIcons.Actions.GroupByModuleGroup
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
                    if (configuration == null) {
                        return;
                    }
                    configuration.setMaxResultBytes(getValue());
                }

                @Override
                protected boolean isVisible(@NotNull AnActionEvent e) {
                    return Boolean.TRUE.equals(e.getData(SHOW_MORE_OPTIONS));
                }
            });
            group.addAction(new AbstractNumericFieldAction(
                    configuration.maxResultRecords(),
                    DQLBundle.message("action.DQL.QueryConfigurationAction.maxRecords.placeholder"),
                    DQLBundle.message("action.DQLExecutionManagerToolbar.option.maxRecords"),
                    AllIcons.Json.Array
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
                    if (configuration == null) {
                        return;
                    }
                    configuration.setMaxResultRecords(getValue());
                }

                @Override
                protected boolean isVisible(@NotNull AnActionEvent e) {
                    return Boolean.TRUE.equals(e.getData(SHOW_MORE_OPTIONS));
                }
            });
        }
    }
}

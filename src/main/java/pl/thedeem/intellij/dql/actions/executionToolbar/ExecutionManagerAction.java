package pl.thedeem.intellij.dql.actions.executionToolbar;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import javax.swing.*;
import java.awt.*;

public class ExecutionManagerAction extends AnAction implements CustomComponentAction {
    private final JComponent myComponent;

    public ExecutionManagerAction(@NotNull PsiFile file) {
        super(DQLBundle.message("action.DQLExecutionManagerToolbar.text"), null, AllIcons.Actions.Execute);
        DQLQueryConfigurationService configService = DQLQueryConfigurationService.getInstance(file.getProject());
        this.myComponent = new DQLExecutionManagerToolbar(configService.getQueryConfiguration(file), file);
    }

    public ExecutionManagerAction(@NotNull DQLExecutionService service) {
        this.myComponent = new DQLExecutionManagerToolbar(service.getConfiguration(), null);
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

    private static final class DQLExecutionManagerToolbar extends JPanel implements UiDataProvider {
        private static final DataKey<Boolean> SHOW_MORE_OPTIONS = DataKey.create("DQL_SHOW_MORE_OPTIONS");
        private final QueryConfiguration configuration;
        private final PsiFile file;
        private boolean showMoreOptions = false;

        public DQLExecutionManagerToolbar(@NotNull QueryConfiguration configuration, @Nullable PsiFile file) {
            this.configuration = configuration;
            this.file = file;
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);

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
            ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DQLExecutionManagerAction", group, true);
            toolbar.setTargetComponent(this);
            JComponent toolbarComponent = toolbar.getComponent();
            toolbarComponent.setOpaque(false);
            toolbarComponent.setBorder(BorderFactory.createEmptyBorder());

            add(toolbarComponent, BorderLayout.WEST);
        }

        @Override
        public void uiDataSnapshot(@NotNull DataSink dataSink) {
            dataSink.set(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION, configuration);
            dataSink.set(SHOW_MORE_OPTIONS, showMoreOptions);
            if (file != null) {
                file.putUserData(DQLQueryConfigurationService.QUERY_CONFIGURATION, configuration);
            }
        }

        private void addQueryConfiguration(@NotNull DefaultActionGroup group) {
            group.add(new AbstractTimeFieldAction(
                    configuration.timeframeStart(),
                    DQLBundle.message("action.DQL.QueryConfigurationAction.queryFrom"),
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
                    DQLBundle.message("action.DQL.QueryConfigurationAction.queryTo"),
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
                    "",
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
                    "",
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
                    "",
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

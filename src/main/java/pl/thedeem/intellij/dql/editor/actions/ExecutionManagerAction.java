package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public class ExecutionManagerAction extends AnAction implements CustomComponentAction {
    private final DQLExecutionManagerToolbar myComponent;
    protected Set<Consumer<AnActionEvent>> listeners = new HashSet<>();

    protected ExecutionManagerAction(boolean showExecuteAction) {
        super(DQLBundle.message("action.DQLExecutionManagerToolbar.text"), null, AllIcons.Actions.Execute);
        this.myComponent = new DQLExecutionManagerToolbar(this, showExecuteAction);
    }

    public ExecutionManagerAction(@NotNull PsiFile file, boolean showExecuteAction) {
        this(showExecuteAction);
        myComponent.init(() -> {
            DQLQueryConfigurationService configService = DQLQueryConfigurationService.getInstance();
            return ReadAction.compute(() -> configService.getQueryConfiguration(file));
        }, file);
    }

    public ExecutionManagerAction(@NotNull DQLExecutionService service) {
        this(true);
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

    public void settingChanged(@Nullable AnActionEvent e) {
        for (Consumer<AnActionEvent> consumer : listeners) {
            consumer.accept(e);
        }
    }

    public void addActionListener(@NotNull Consumer<AnActionEvent> l) {
        listeners.add(l);
    }

    public void removeActionListener(@NotNull Consumer<AnActionEvent> l) {
        listeners.remove(l);
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        return myComponent;
    }

    private static final class DQLExecutionManagerToolbar extends BorderLayoutPanel implements UiDataProvider {
        private static final DataKey<Boolean> SHOW_MORE_OPTIONS = DataKey.create("DQL_SHOW_MORE_OPTIONS");
        private final ExecutionManagerAction manager;
        private final boolean showExecuteAction;
        private QueryConfiguration configuration;
        private PsiFile file;
        private boolean showMoreOptions = false;

        public DQLExecutionManagerToolbar(@NotNull ExecutionManagerAction manager, boolean showExecuteAction) {
            this.manager = manager;
            this.showExecuteAction = showExecuteAction;
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
            if (showExecuteAction) {
                group.add(actionManager.getAction("DQL.StartStopExecution"));
                group.addSeparator();
            }
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
            toolbarComponent.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    SwingUtilities.invokeLater(() -> manager.settingChanged(null));
                }
            });
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
                    manager.settingChanged(e);
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
                    manager.settingChanged(e);
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
                public void setSelected(@NotNull AnActionEvent e, boolean selected) {
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
                    manager.settingChanged(e);
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
                    manager.settingChanged(e);
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
                    manager.settingChanged(e);
                }

                @Override
                protected boolean isVisible(@NotNull AnActionEvent e) {
                    return Boolean.TRUE.equals(e.getData(SHOW_MORE_OPTIONS));
                }
            });
        }
    }
}

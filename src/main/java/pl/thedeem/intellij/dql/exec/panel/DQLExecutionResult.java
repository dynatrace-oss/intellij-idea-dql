package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.icons.AllIcons;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.common.components.FormattedLanguageText;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;

import javax.swing.*;
import java.awt.*;
import java.time.ZonedDateTime;
import java.util.Objects;

public class DQLExecutionResult extends BorderLayoutPanel {
    private final DQLExecuteInProgressPanel progressPanel;
    private final Project project;
    private final DefaultActionGroup customActions;
    private final DefaultActionGroup panelToolbar;

    private DQLPollResponse response;
    private Exception exception;
    private ResultsDisplayMode displayMode;
    private QueryConfiguration params;
    private ZonedDateTime executionTime;

    public DQLExecutionResult(@NotNull Project project) {
        this.setOpaque(false);
        this.setBorder(JBUI.Borders.empty());
        this.project = project;
        this.progressPanel = new DQLExecuteInProgressPanel();
        this.customActions = new DefaultActionGroup();
        this.panelToolbar = createPanelToolbar();
        show(progressPanel);
    }

    private DefaultActionGroup createPanelToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.addAction(createModeToggleAction(
                ResultsDisplayMode.TABLE,
                DQLBundle.message("action.DQL.ResultsAsTableAction.text"),
                AllIcons.Nodes.DataTables
        ));
        group.addAction(createModeToggleAction(
                ResultsDisplayMode.JSON,
                DQLBundle.message("action.DQL.ResultsAsJsonAction.text"),
                AllIcons.FileTypes.Json
        ));
        group.addAction(createModeToggleAction(
                ResultsDisplayMode.METADATA,
                DQLBundle.message("action.DQL.ShowQueryMetadata.text"),
                AllIcons.Actions.Annotate
        ));
        group.addAction(createModeToggleAction(
                ResultsDisplayMode.USED_QUERY,
                DQLBundle.message("action.DQL.ShowUsedDQLQuery.text"),
                AllIcons.Actions.Preview
        ));
        group.addSeparator();
        group.add(this.customActions);
        return group;
    }

    public void show(@NotNull Component comp, @NotNull AnAction... actions) {
        this.removeAll();
        this.addToCenter(comp);
        this.revalidate();
        this.repaint();
        this.customActions.removeAll();
        if (actions != null) {
            for (AnAction action : actions) {
                this.customActions.add(action);
            }
        }
    }

    public void setQueryConfiguration(@NotNull QueryConfiguration params) {
        this.params = params;
    }

    public void setExecutionTime(@NotNull ZonedDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public void update(@NotNull DQLExecuteResponse response) {
        progressPanel.update(response);
    }

    public void update(@NotNull DQLPollResponse response) {
        this.response = response;
        this.exception = null;
        if (response.isFinished()) {
            refreshView();
        } else {
            progressPanel.update(response);
        }
    }

    public void update(@NotNull Exception exception) {
        this.exception = exception;
        refreshView();
    }

    public void setDisplayMode(@NotNull ResultsDisplayMode displayMode) {
        this.displayMode = displayMode;
        refreshView();
    }

    private void refreshView() {
        ResultsDisplayMode displayMode = getDisplayMode();
        if (displayMode == ResultsDisplayMode.USED_QUERY) {
            FormattedLanguageText panel = new FormattedLanguageText(DynatraceQueryLanguage.INSTANCE, project, true);
            show(panel);
            if (response != null && response.getResult() != null && response.getResult().getGrailMetadata() != null) {
                panel.showResult(() -> response.getResult().getGrailMetadata().getQuery());
            } else if (params != null) {
                panel.showResult(() -> params.query());
            }
            return;
        }
        if (this.exception != null) {
            show(new DQLExecutionErrorPanel(this.exception));
            return;
        }
        if (this.response == null) {
            return;
        }
        switch (displayMode) {
            case JSON -> {
                FormattedLanguageText panel = new FormattedLanguageText(JsonLanguage.INSTANCE, project, true);
                show(panel);
                panel.showResult(
                        () -> IntelliJUtils.prettyPrintJson(response.getResult().getRecords())
                );
            }
            case METADATA -> show(new DQLMetadataPanel(response.getResult().getGrailMetadata(), executionTime));
            default -> {
                DQLTableResultPanel panel = new DQLTableResultPanel(response, project);
                show(panel, new AnAction(DQLBundle.message("runConfiguration.executeDQL.changeColumnsList"), null, AllIcons.Actions.PreviewDetails) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e) {
                        panel.showColumnSettingsPopup(e);
                    }
                });
            }
        }
        addToBottom(new TransparentScrollPane(new DQLExecutionSummary(response.getResult(), executionTime)));
    }

    public @NotNull ResultsDisplayMode getDisplayMode() {
        return Objects.requireNonNullElse(this.displayMode, ResultsDisplayMode.TABLE);
    }

    public @NotNull ActionGroup getToolbarActions() {
        return panelToolbar;
    }

    private @NotNull AnAction createModeToggleAction(@NotNull ResultsDisplayMode mode, @NotNull String text, @NotNull Icon icon) {
        return new ToggleAction(text, null, icon) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                return getDisplayMode() == mode;
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean b) {
                if (getDisplayMode() != mode) {
                    setDisplayMode(mode);
                }
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
    }

    public enum ResultsDisplayMode {
        JSON,
        TABLE,
        USED_QUERY,
        METADATA
    }
}

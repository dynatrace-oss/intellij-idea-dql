package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.icons.AllIcons;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.Icons;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.common.components.FormattedLanguageText;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;

import javax.swing.*;
import java.awt.*;
import java.time.ZonedDateTime;
import java.util.Objects;

public class DQLExecutionResult extends BorderLayoutPanel {
    private final Project project;
    private final DefaultActionGroup customActions;
    private final DefaultActionGroup panelToolbar;
    private final DQLResult result;
    private final ZonedDateTime executionTime;
    private final QueryConfiguration params;

    private final FormattedLanguageText queryPanel;
    private final FormattedLanguageText jsonPanel;
    private final DQLVisualizationPanel visualizationPanel;
    private final DQLMetadataPanel metadataPanel;
    private final DQLTableResultPanel tablePanel;

    private ResultsDisplayMode displayMode;

    public DQLExecutionResult(@NotNull Project project, @NotNull DQLResult result, @Nullable ZonedDateTime executionTime, @Nullable QueryConfiguration params) {
        this.result = result;
        this.project = project;
        this.executionTime = executionTime;
        this.params = params;

        this.customActions = new DefaultActionGroup();
        this.panelToolbar = createPanelToolbar();
        this.queryPanel = new FormattedLanguageText(DynatraceQueryLanguage.INSTANCE, project, true);
        this.jsonPanel = new FormattedLanguageText(JsonLanguage.INSTANCE, project, true);
        this.visualizationPanel = new DQLVisualizationPanel(result);
        this.metadataPanel = new DQLMetadataPanel(result.getGrailMetadata(), executionTime);
        this.tablePanel = new DQLTableResultPanel(result, project);

        this.setOpaque(false);
        this.setBorder(JBUI.Borders.empty());
        refreshView();
    }

    private DefaultActionGroup createPanelToolbar() {
        DefaultActionGroup group = new DefaultActionGroup();
        group.addAction(createModeToggleAction(
                ResultsDisplayMode.TABLE,
                DQLBundle.message("components.executionResult.actions.tableView.title"),
                AllIcons.Nodes.DataTables
        ));
        group.addAction(createModeToggleAction(
                ResultsDisplayMode.JSON,
                DQLBundle.message("components.executionResult.actions.jsonView.title"),
                AllIcons.FileTypes.Json
        ));
        group.addAction(createModeToggleAction(
                ResultsDisplayMode.VISUALIZATION,
                DQLBundle.message("components.executionResult.actions.visualizationView.title"),
                Icons.PIE_CHART
        ));
        group.addAction(createModeToggleAction(
                ResultsDisplayMode.METADATA,
                DQLBundle.message("components.executionResult.actions.metadataView.title"),
                AllIcons.Actions.Annotate
        ));
        group.addAction(createModeToggleAction(
                ResultsDisplayMode.USED_QUERY,
                DQLBundle.message("components.executionResult.actions.dqlQueryView.title"),
                AllIcons.Actions.Preview
        ));
        group.addAction(new AnAction(
                DQLBundle.message("components.executionResult.actions.saveToFile.title"),
                null,
                AllIcons.Actions.Install
        ) {
            private final static String DEFAULT_FILE_NAME = "dql-result.json";

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }

            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                IntelliJUtils.openSaveFileDialog(
                        DQLBundle.message("components.executionResult.actions.saveToFile.dialogTitle"),
                        DQLBundle.message("components.executionResult.actions.saveToFile.dialogDescription"),
                        getFileName(),
                        () -> IntelliJUtils.prettyPrintJson(result.getRecords()),
                        project
                );
            }

            private @NotNull String getFileName() {
                if (result.getGrailMetadata() == null || result.getGrailMetadata().queryId == null) {
                    return DEFAULT_FILE_NAME;
                }
                return result.getGrailMetadata().queryId + ".json";
            }
        });
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
        for (AnAction action : actions) {
            this.customActions.add(action);
        }
    }


    public void setDisplayMode(@NotNull ResultsDisplayMode displayMode) {
        this.displayMode = displayMode;
        refreshView();
    }

    private void refreshView() {
        ResultsDisplayMode displayMode = getDisplayMode();
        switch (displayMode) {
            case USED_QUERY -> {
                show(queryPanel);
                if (result.getGrailMetadata() != null) {
                    queryPanel.showResult(() -> result.getGrailMetadata().getQuery());
                } else if (params != null) {
                    queryPanel.showResult(() -> params.query());
                }
            }
            case JSON -> {
                show(jsonPanel);
                jsonPanel.showResult(() -> IntelliJUtils.prettyPrintJson(result.getRecords()));
            }
            case VISUALIZATION -> show(visualizationPanel, visualizationPanel.getToolbarActions());
            case METADATA -> show(metadataPanel);
            default -> show(tablePanel, tablePanel.getToolbarActions());
        }
        addToBottom(new TransparentScrollPane(new DQLExecutionSummary(result, executionTime)));
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
        VISUALIZATION,
        USED_QUERY,
        METADATA
    }
}

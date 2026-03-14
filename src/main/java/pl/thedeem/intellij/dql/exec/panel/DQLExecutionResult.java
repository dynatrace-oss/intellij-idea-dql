package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.icons.AllIcons;
import com.intellij.json.JsonLanguage;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBCardLayout;
import com.intellij.ui.components.JBPanel;
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
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;

import javax.swing.*;
import java.awt.*;
import java.time.ZonedDateTime;

public class DQLExecutionResult extends BorderLayoutPanel {
    private final Project project;
    private final DefaultActionGroup customActions;
    private final DefaultActionGroup panelToolbar;
    private final DQLResult result;
    private final QueryConfiguration params;

    private final FormattedLanguageText queryPanel;
    private final FormattedLanguageText jsonPanel;
    private final DQLVisualizationPanel visualizationPanel;
    private final DQLTableResultPanel tablePanel;

    private final JBCardLayout cardLayout;
    private final JBPanel<?> cardPanel;

    public DQLExecutionResult(@NotNull Project project, @NotNull DQLResult result, @Nullable ZonedDateTime executionTime, @Nullable QueryConfiguration params) {
        withBorder(JBUI.Borders.empty()).andTransparent();

        this.result = result;
        this.project = project;
        this.params = params;

        this.customActions = new DefaultActionGroup();
        this.panelToolbar = createPanelToolbar();
        this.queryPanel = new FormattedLanguageText(DynatraceQueryLanguage.INSTANCE, project, true);
        this.jsonPanel = new FormattedLanguageText(JsonLanguage.INSTANCE, project, true);
        this.visualizationPanel = new DQLVisualizationPanel(result);
        this.tablePanel = new DQLTableResultPanel(result, project);

        this.cardLayout = new JBCardLayout();
        this.cardPanel = new JBPanel<>(cardLayout).andTransparent();
        this.cardPanel.add(tablePanel, ResultsDisplayMode.TABLE.name());
        this.cardPanel.add(jsonPanel, ResultsDisplayMode.JSON.name());
        this.cardPanel.add(visualizationPanel, ResultsDisplayMode.VISUALIZATION.name());
        this.cardPanel.add(new DQLMetadataPanel(result.getGrailMetadata(), executionTime), ResultsDisplayMode.METADATA.name());
        this.cardPanel.add(queryPanel, ResultsDisplayMode.USED_QUERY.name());

        this.addToCenter(cardPanel);
        this.addToBottom(new TransparentScrollPane(new DQLExecutionSummary(result, executionTime)));
        setDisplayMode(ResultsDisplayMode.TABLE);
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
                DQLIcon.SHOW_QUERY
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

    public void setDisplayMode(@NotNull ResultsDisplayMode displayMode) {
        cardLayout.show(cardPanel, displayMode.name());
        customActions.removeAll();
        switch (displayMode) {
            case USED_QUERY -> {
                if (result.getGrailMetadata() != null) {
                    queryPanel.showResult(() -> result.getGrailMetadata().getQuery());
                } else if (params != null) {
                    queryPanel.showResult(params::query);
                }
            }
            case JSON -> jsonPanel.showResult(() -> IntelliJUtils.prettyPrintJson(result.getRecords()));
            case VISUALIZATION -> {
                for (AnAction action : visualizationPanel.getToolbarActions()) {
                    customActions.add(action);
                }
            }
            case TABLE -> {
                for (AnAction action : tablePanel.getToolbarActions()) {
                    customActions.add(action);
                }
            }
            default -> {
            }
        }
    }

    public @NotNull ActionGroup getToolbarActions() {
        return panelToolbar;
    }

    private @NotNull AnAction createModeToggleAction(@NotNull ResultsDisplayMode mode, @NotNull String text, @NotNull Icon icon) {
        return new ToggleAction(text, null, icon) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent e) {
                Component found = cardLayout.findComponentById(mode.name());
                return found != null && found.isVisible();
            }

            @Override
            public void setSelected(@NotNull AnActionEvent e, boolean b) {
                Component found = cardLayout.findComponentById(mode.name());
                if (found == null || !found.isVisible()) {
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

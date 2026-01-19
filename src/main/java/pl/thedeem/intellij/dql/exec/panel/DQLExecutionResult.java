package pl.thedeem.intellij.dql.exec.panel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.FormattedLanguageText;
import pl.thedeem.intellij.common.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;

import java.awt.*;

public class DQLExecutionResult extends BorderLayoutPanel {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final DQLExecuteInProgressPanel progressPanel;
    private final Project project;

    private DQLPollResponse response;
    private Exception exception;
    private DQLExecutionService.ResultsDisplayMode displayMode;
    private QueryConfiguration params;

    public DQLExecutionResult(@NotNull Project project) {
        this.setOpaque(false);
        this.setBorder(JBUI.Borders.empty());
        this.project = project;
        this.progressPanel = new DQLExecuteInProgressPanel();
        show(progressPanel);
    }

    public void show(@NotNull Component comp) {
        this.removeAll();
        this.addToCenter(comp);
        this.revalidate();
        this.repaint();
    }

    public void update(@NotNull QueryConfiguration params) {
        this.params = params;
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

    public void setDisplayMode(@NotNull DQLExecutionService.ResultsDisplayMode displayMode) {
        this.displayMode = displayMode;
        refreshView();
    }

    private void refreshView() {
        if (displayMode == DQLExecutionService.ResultsDisplayMode.USED_QUERY) {
            FormattedLanguageText panel = new FormattedLanguageText(true);
            show(panel);
            if (params != null) {
                panel.showResult(params.query(), DQLFileType.INSTANCE, project);
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
                FormattedLanguageText panel = new FormattedLanguageText(true);
                show(panel);
                try {
                    panel.showResult(mapper.writeValueAsString(response.getResult()), JsonFileType.INSTANCE, project);
                } catch (JsonProcessingException e) {
                    show(new DQLExecutionErrorPanel(e));
                }
            }
            case METADATA -> show(new DQLMetadataPanel(response.getResult().getGrailMetadata()));
            case null, default -> show(new DQLTableResultPanel(response, project));
        }
        addToBottom(createTableSummary(response.getResult()));
    }

    private JBScrollPane createTableSummary(DQLResult result) {
        DQLExecutionSummary summaryPanel = new DQLExecutionSummary(result);
        JBScrollPane res = new JBScrollPane(summaryPanel);
        res.setBorder(JBUI.Borders.empty());
        return res;
    }

    public @Nullable DQLExecutionService.ResultsDisplayMode getDisplayMode() {
        return this.displayMode;
    }
}

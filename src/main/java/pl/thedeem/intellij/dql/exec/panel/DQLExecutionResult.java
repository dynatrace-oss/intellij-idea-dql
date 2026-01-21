package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.common.components.FormattedLanguageText;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;

import java.awt.*;
import java.time.ZonedDateTime;
import java.util.Objects;

public class DQLExecutionResult extends BorderLayoutPanel {


    private final DQLExecuteInProgressPanel progressPanel;
    private final Project project;

    private DQLPollResponse response;
    private Exception exception;
    private DQLExecutionService.ResultsDisplayMode displayMode;
    private QueryConfiguration params;
    private ZonedDateTime executionTime;

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

    public void setDisplayMode(@NotNull DQLExecutionService.ResultsDisplayMode displayMode) {
        this.displayMode = displayMode;
        refreshView();
    }

    private void refreshView() {
        DQLExecutionService.ResultsDisplayMode displayMode = getDisplayMode();
        if (displayMode == DQLExecutionService.ResultsDisplayMode.USED_QUERY) {
            FormattedLanguageText panel = new FormattedLanguageText(DynatraceQueryLanguage.INSTANCE, project, true);
            show(panel);
            if (params != null) {
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
            default -> show(new DQLTableResultPanel(response, project));
        }
        addToBottom(new TransparentScrollPane(new DQLExecutionSummary(response.getResult(), executionTime)));
    }

    public @NotNull DQLExecutionService.ResultsDisplayMode getDisplayMode() {
        return Objects.requireNonNullElse(this.displayMode, DQLExecutionService.ResultsDisplayMode.TABLE);
    }
}

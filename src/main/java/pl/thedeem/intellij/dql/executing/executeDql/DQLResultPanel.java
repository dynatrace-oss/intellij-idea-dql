package pl.thedeem.intellij.dql.executing.executeDql;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.components.DQLExecutionTableResults;
import pl.thedeem.intellij.dql.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.dql.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.sdk.model.errors.DQLAuthErrorResponse;
import pl.thedeem.intellij.dql.sdk.model.errors.DQLErrorResponse;
import pl.thedeem.intellij.dql.sdk.model.errors.DQLExecutionErrorResponse;

import javax.swing.*;
import java.awt.*;

public class DQLResultPanel extends JPanel {
    private final JPanel progressPanel;
    private final JProgressBar progressBar;
    private final JPanel resultsPanel;

    public DQLResultPanel() {
        setLayout(new BorderLayout());

        progressBar = new JProgressBar();
        progressBar.setString(DQLBundle.message("runConfiguration.executeDQL.panel.progress.start"));
        progressBar.setStringPainted(true);
        progressPanel = new JPanel(new GridBagLayout());
        progressPanel.add(progressBar);
        progressBar.setIndeterminate(true);
        resultsPanel = new JPanel(new BorderLayout());
    }

    public void registerProgress(@NotNull DQLExecuteResponse executedResponse) {
        progressBar.setIndeterminate(false);
        removeAll();
        add(progressPanel, BorderLayout.CENTER);

        updateProgressBar(0, executedResponse.getState());
        revalidate();
        repaint();
    }

    public void registerProgress(@NotNull DQLPollResponse dqlResponse) {
        progressBar.setIndeterminate(false);
        updateProgressBar(dqlResponse.progress.intValue(), dqlResponse.state);
        if (dqlResponse.isFinished()) {
            resultsPanel.removeAll();
            resultsPanel.add(new DQLExecutionTableResults(dqlResponse.result), BorderLayout.CENTER);
            removeAll();
            add(resultsPanel, BorderLayout.CENTER);
        }
        revalidate();
        repaint();
    }

    public void updateProgressBar(int progress, String state) {
        progressBar.setValue(progress);
        progressBar.setString(state);
    }

    public void showError(@Nullable DQLErrorResponse<?> response, Exception e) {
        JPanel errorPanel = new JPanel(new GridBagLayout());
        String message = DQLBundle.message("runConfiguration.executeDQL.errors.unknown", e.getMessage());
        if (response != null) {
            if (response.error instanceof DQLAuthErrorResponse error) {
                String details = error.message;
                if (error.details.get("errorMessage") instanceof String msg) {
                    details = msg;
                }
                message = DQLBundle.message("runConfiguration.executeDQL.errors.unauthorized", details);
            } else if (response.error instanceof DQLExecutionErrorResponse error) {
                String details = error.message;
                if (error.details.errorMessage instanceof String msg) {
                    details = msg;
                }
                message = DQLBundle.message("runConfiguration.executeDQL.errors.execution", details);
            }
        }
        errorPanel.setBorder(BorderFactory.createEmptyBorder());
        errorPanel.add(new JLabel(message, AllIcons.General.Error, JLabel.LEFT));
        resultsPanel.removeAll();
        resultsPanel.add(new JBScrollPane(errorPanel), BorderLayout.CENTER);
        removeAll();
        add(resultsPanel, BorderLayout.CENTER);
    }
}

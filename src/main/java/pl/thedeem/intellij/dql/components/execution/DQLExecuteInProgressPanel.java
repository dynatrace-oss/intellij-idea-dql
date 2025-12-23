package pl.thedeem.intellij.dql.components.execution;

import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;

public class DQLExecuteInProgressPanel extends JPanel {
    private final JProgressBar progressBar = new JProgressBar();

    public DQLExecuteInProgressPanel() {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());

        progressBar.setString(DQLBundle.message("runConfiguration.executeDQL.panel.progress.start"));
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        add(progressBar);
    }

    public DQLExecuteInProgressPanel(@NotNull DQLExecuteResponse response) {
        this();
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString(response.getState());
    }


    public DQLExecuteInProgressPanel(@NotNull DQLPollResponse response) {
        this();
        progressBar.setIndeterminate(false);
        progressBar.setValue(response.getProgress() != null ? response.getProgress().intValue() : 0);
        progressBar.setString(response.getState());
    }
}

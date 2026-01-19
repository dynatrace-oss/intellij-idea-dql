package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.util.ui.JBUI;
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
        setBorder(JBUI.Borders.empty());

        progressBar.setString(DQLBundle.message("runConfiguration.executeDQL.panel.progress.start"));
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        add(progressBar);
    }

    public void update(@NotNull DQLExecuteResponse response) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(0);
        progressBar.setString(response.getState());
    }

    public void update(@NotNull DQLPollResponse response) {
        progressBar.setIndeterminate(false);
        progressBar.setValue(response.getProgress() != null ? response.getProgress().intValue() : 0);
        progressBar.setString(response.getState());
    }
}

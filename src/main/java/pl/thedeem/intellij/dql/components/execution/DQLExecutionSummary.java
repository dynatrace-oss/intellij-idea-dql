package pl.thedeem.intellij.dql.components.execution;

import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.components.ComponentsUtils;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;

public class DQLExecutionSummary extends JPanel {
    private final JLabel summary;
    private final DQLResult result;

    public DQLExecutionSummary(@NotNull DQLResult result) {
        super(new FlowLayout(FlowLayout.LEFT));
        this.result = result;
        setBorder(BorderFactory.createEmptyBorder());
        summary = new JLabel(DQLBundle.message(
                "components.dqlResults.summary.description",
                result.getRecords().size(),
                result.getGrailMetadata().executionTimeMilliseconds
        ), AllIcons.General.Information, JLabel.LEFT);

        summary.setBorder(ComponentsUtils.DEFAULT_BORDER);

        add(summary, BorderLayout.WEST);

        for (DQLResult.DQLNotification notification : result.getGrailMetadata().getNotifications()) {
            JPanel notificationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            notificationPanel.setBorder(BorderFactory.createEmptyBorder());
            notificationPanel.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
            notificationPanel.add(new JLabel(notification.getMessage(), AllIcons.General.Warning, JLabel.LEFT), BorderLayout.EAST);
            add(notificationPanel, BorderLayout.EAST);
        }
    }

    public void refreshDescription() {
        summary.setText(DQLBundle.message(
                "components.dqlResults.summary.description",
                result.getRecords().size(),
                result.getGrailMetadata().executionTimeMilliseconds
        ));
    }
}
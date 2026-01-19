package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.components.ComponentsUtils;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;

public class DQLExecutionSummary extends JPanel {
    public DQLExecutionSummary(@NotNull DQLResult result) {
        super(new FlowLayout(FlowLayout.LEFT));
        setBorder(JBUI.Borders.empty());
        JBLabel summary = new JBLabel(DQLBundle.message(
                "components.dqlResults.summary.description",
                result.getRecords().size(),
                result.getGrailMetadata().executionTimeMilliseconds
        ), AllIcons.General.Information, JLabel.LEFT);

        summary.setBorder(ComponentsUtils.DEFAULT_BORDER);

        add(summary, BorderLayout.WEST);

        for (DQLResult.DQLNotification notification : result.getGrailMetadata().getNotifications()) {
            JPanel notificationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            notificationPanel.setBorder(JBUI.Borders.empty());
            notificationPanel.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
            notificationPanel.add(new JBLabel(notification.getMessage(), AllIcons.General.Warning, JLabel.LEFT), BorderLayout.EAST);
            add(notificationPanel, BorderLayout.EAST);
        }
    }
}
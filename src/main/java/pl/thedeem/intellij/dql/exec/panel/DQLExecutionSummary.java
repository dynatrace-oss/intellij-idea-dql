package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.ComponentsUtils;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.ZonedDateTime;

public class DQLExecutionSummary extends JPanel {
    public DQLExecutionSummary(@NotNull DQLResult result, @Nullable ZonedDateTime executionTime) {
        super(new FlowLayout(FlowLayout.LEFT));
        setBorder(JBUI.Borders.empty());
        JBLabel summary = new JBLabel(DQLBundle.message(
                "components.executionSummary.simpleSummary.title",
                result.getRecords().size(),
                result.getGrailMetadata().executionTimeMilliseconds
        ), AllIcons.General.Information, JLabel.LEFT);

        summary.setBorder(ComponentsUtils.DEFAULT_BORDER);

        installLazyRelativeTimeTooltip(summary, executionTime);

        add(summary);

        for (DQLResult.DQLNotification notification : result.getGrailMetadata().getNotifications()) {
            add(new JSeparator(SwingConstants.VERTICAL));
            add(new JBLabel(notification.getMessage(), AllIcons.General.Warning, JLabel.LEFT));
        }
    }

    private static void installLazyRelativeTimeTooltip(@NotNull JComponent component, @Nullable ZonedDateTime executionTime) {
        if (executionTime == null) {
            return;
        }
        component.setToolTipText(null);
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                component.setToolTipText(formatRelativeTime(executionTime));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                component.setToolTipText(null);
            }
        });
    }

    private static @NotNull String formatRelativeTime(@NotNull ZonedDateTime executionTime) {
        ZonedDateTime now = ZonedDateTime.now(executionTime.getZone());
        Duration duration = Duration.between(executionTime, now);
        return DQLBundle.message("components.executionSummary.simpleSummary.tooltip", formatDuration(duration));
    }

    private static @NotNull String formatDuration(@NotNull Duration duration) {
        long seconds = Math.max(0L, duration.getSeconds());
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h";
        }
        long days = hours / 24;
        return days + "d";
    }
}
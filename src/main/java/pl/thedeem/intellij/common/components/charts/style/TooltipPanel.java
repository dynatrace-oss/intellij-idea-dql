package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

class TooltipPanel extends JBPanel<TooltipPanel> {
    private static final Color TOOLTIP_BACKGROUND = JBColor.namedColor("ToolTip.background", JBColor.background());
    private static final Color TOOLTIP_BORDER = JBColor.namedColor("ToolTip.borderColor", new JBColor(0xC4C4C4, 0x555555));
    private final int squareSize = getFontMetrics(JBUI.Fonts.label()).getAscent();

    TooltipPanel() {
        super(new MigLayout("insets 4, gap 4", "[][]"));
        withBackground(TOOLTIP_BACKGROUND)
                .withBorder(JBUI.Borders.empty(JBUI.scale(3)))
                .andOpaque();
        setVisible(false);
    }

    void showTooltip(
            @NotNull ChartHitPoint hitPoint,
            @NotNull Point2D point,
            int panelWidth
    ) {
        removeAll();
        add(new JBLabel(DQLBundle.message("components.visualization.tooltip.title", hitPoint.getDomainLabel()))
                        .withFont(JBUI.Fonts.label().asBold()),
                "span 2, wrap");
        add(new JSeparator(), "span 2, growx, wrap");
        add(new JBPanel<>()
                .withPreferredSize(squareSize, squareSize)
                .withBorder(BorderFactory.createLineBorder(TOOLTIP_BORDER))
                .withBackground(hitPoint.seriesPaint() instanceof Color c ? c : JBColor.BLUE)
                .andOpaque());
        add(new JBLabel(DQLBundle.message("components.visualization.tooltip.value",
                hitPoint.getSeriesName(),
                formatValue(hitPoint.getYValue())
        )));
        setVisible(true);
        revalidate();
        recalculateBounds(point, panelWidth);
        repaint();
    }

    private void recalculateBounds(@NotNull Point2D point, int panelWidth) {
        Dimension preferred = getPreferredSize();
        double bx = point.getX() + 10;
        double by = point.getY() - 8 - preferred.height;

        if (bx + preferred.width > panelWidth) {
            bx = point.getX() - preferred.width - 10;
        }
        if (by < 0) {
            by = point.getY() + 10;
        }

        setBounds((int) bx, (int) by, preferred.width, preferred.height);
    }

    private static @NotNull String formatValue(double value) {
        if (value == Math.floor(value) && Double.isFinite(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.4g", value);
    }
}

package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

class TooltipPanel extends JPanel {
    private static final Color TOOLTIP_BACKGROUND = JBColor.namedColor("ToolTip.background", JBColor.background());
    private static final Color TOOLTIP_BORDER = JBColor.namedColor("ToolTip.borderColor", new JBColor(0xC4C4C4, 0x555555));

    private final JBLabel titleLabel = new JBLabel();
    private final JBLabel valueLabel = new JBLabel();
    private final JPanel seriesSquare = new JPanel();

    TooltipPanel() {
        setLayout(new MigLayout("insets 4, gap 4", "[][]"));
        setOpaque(true);
        setVisible(false);
        setBackground(TOOLTIP_BACKGROUND);
        setBorder(JBUI.Borders.empty(JBUI.scale(3)));

        titleLabel.setFont(JBUI.Fonts.label().asBold());

        int squareSize = getFontMetrics(JBUI.Fonts.label()).getAscent();
        seriesSquare.setPreferredSize(new Dimension(squareSize, squareSize));
        seriesSquare.setBorder(BorderFactory.createLineBorder(TOOLTIP_BORDER));
        seriesSquare.setOpaque(true);
        
        add(titleLabel, "span 2, wrap");
        add(new JSeparator(), "span 2, growx, wrap");
        add(seriesSquare);
        add(valueLabel);
    }

    void showTooltip(
            @NotNull HoverOverlay.HitPoint hitPoint,
            @NotNull Point2D point,
            int panelWidth
    ) {
        titleLabel.setText(DQLBundle.message("components.visualization.tooltip.title", hitPoint.getDomainLabel()));
        valueLabel.setText(DQLBundle.message("components.visualization.tooltip.value",
                hitPoint.getSeriesName(),
                formatValue(hitPoint.getYValue())
        ));

        Paint seriesColor = hitPoint.seriesPaint();
        seriesSquare.setBackground(seriesColor instanceof Color c ? c : JBColor.BLUE);

        setVisible(true);
        revalidate();

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
        repaint();
    }

    void hideTooltip() {
        setVisible(false);
        valueLabel.setText(null);
    }

    private static @NotNull String formatValue(double value) {
        if (value == Math.floor(value) && Double.isFinite(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.4g", value);
    }
}

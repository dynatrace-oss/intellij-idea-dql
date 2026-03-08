package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

class TooltipPanel extends JPanel {
    private static final Color TOOLTIP_BACKGROUND = JBColor.namedColor("ToolTip.background", JBColor.background());
    private static final Color TOOLTIP_BORDER = JBColor.namedColor("ToolTip.borderColor", new JBColor(0xC4C4C4, 0x555555));

    private final JPanel seriesSquare = new JPanel();
    private final JLabel valueLabel = new JLabel();

    TooltipPanel() {
        setLayout(new MigLayout("insets 4, gap 4", "[][]"));
        setOpaque(true);
        setVisible(false);
        setBackground(TOOLTIP_BACKGROUND);
        setBorder(JBUI.Borders.empty(JBUI.scale(3)));

        int squareSize = getFontMetrics(JBUI.Fonts.label()).getAscent();
        seriesSquare.setPreferredSize(new Dimension(squareSize, squareSize));
        seriesSquare.setBorder(BorderFactory.createLineBorder(TOOLTIP_BORDER));
        seriesSquare.setOpaque(true);

        valueLabel.setFont(JBUI.Fonts.label());
        valueLabel.setForeground(JBColor.namedColor("ToolTip.foreground", JBColor.foreground()));

        add(seriesSquare);
        add(valueLabel);
    }

    void showTooltip(@NotNull String label, @Nullable Paint seriesColor, int x, int y, int panelWidth) {
        valueLabel.setText(label);
        seriesSquare.setBackground(seriesColor instanceof Color c ? c : JBColor.BLUE);
        seriesSquare.setVisible(seriesColor != null);

        setVisible(true);
        revalidate();

        Dimension preferred = getPreferredSize();
        int bx = x + 10;
        int by = y - 8 - preferred.height;

        if (bx + preferred.width > panelWidth) {
            bx = x - preferred.width - 10;
        }
        if (by < 0) {
            by = y + 10;
        }

        setBounds(bx, by, preferred.width, preferred.height);
        repaint();
    }

    void hideTooltip() {
        setVisible(false);
        valueLabel.setText(null);
    }
}

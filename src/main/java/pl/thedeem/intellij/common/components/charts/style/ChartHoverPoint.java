package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Ellipse2D;

class ChartHoverPoint extends JBPanel<ChartHoverPoint> {
    private static final int SIZE = JBUI.scale(12);
    private static final Stroke STROKE = new BasicStroke(1.5f);

    private @Nullable Paint seriesPaint;

    ChartHoverPoint() {
        setOpaque(false);
        Dimension size = new Dimension(SIZE, SIZE);
        setPreferredSize(size);
        setSize(size);
        setVisible(false);
    }

    void show(@NotNull Point center, @NotNull Paint paint) {
        seriesPaint = paint;
        setLocation(center.x - SIZE / 2, center.y - SIZE / 2);
        setVisible(true);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (seriesPaint == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int margin = (int) Math.ceil(((BasicStroke) STROKE).getLineWidth());
            Ellipse2D shape = new Ellipse2D.Double(margin, margin, getWidth() - margin * 2.0 - 1, getHeight() - margin * 2.0 - 1);
            g2.setStroke(STROKE);
            g2.setPaint(seriesPaint);
            g2.fill(shape);
            g2.setPaint(JBColor.foreground());
            g2.draw(shape);
        } finally {
            g2.dispose();
        }
    }
}

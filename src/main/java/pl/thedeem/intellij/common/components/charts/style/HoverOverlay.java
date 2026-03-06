package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.panel.AbstractOverlay;
import org.jfree.chart.panel.Overlay;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class HoverOverlay extends AbstractOverlay implements Overlay {
    private static final int HIT_TOLERANCE_PX = 20;
    private static final Shape HIGHLIGHT_SHAPE = new Ellipse2D.Double(-5.0, -5.0, 10, 10);
    private static final Stroke HIGHLIGHT_STROKE = new BasicStroke(1.5f);

    private final TooltipPanel tooltipPanel;
    private @Nullable HitPoint current;

    HoverOverlay(@NotNull TooltipPanel tooltipPanel) {
        this.tooltipPanel = tooltipPanel;
    }

    void update(@NotNull ChartPanel chartPanel, @NotNull Point2D mouseJava2D) {
        Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
        if (dataArea.contains(mouseJava2D)) {
            current = findClosestHitPoint(chartPanel, mouseJava2D, dataArea);
            if (current != null) {
                Point2D screen = chartPanel.translateJava2DToScreen(current.screenPoint());
                JRootPane rootPane = SwingUtilities.getRootPane(chartPanel);
                if (rootPane != null) {
                    Point converted = SwingUtilities.convertPoint(chartPanel,
                            (int) screen.getX(), (int) screen.getY(), rootPane.getLayeredPane());
                    tooltipPanel.showTooltip(current.label(), current.seriesColor(),
                            converted.x, converted.y, rootPane.getLayeredPane().getWidth());
                }
            } else {
                tooltipPanel.hideTooltip();
            }
        } else {
            clear();
        }
    }

    void clear() {
        current = null;
        tooltipPanel.hideTooltip();
    }

    @Override
    public void paintOverlay(@NotNull Graphics2D g2, @NotNull ChartPanel chartPanel) {
        if (current == null) return;

        Point2D anchor = chartPanel.translateJava2DToScreen(current.screenPoint());
        Graphics2D g = (Graphics2D) g2.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(anchor.getX(), anchor.getY());
            g.setStroke(HIGHLIGHT_STROKE);
            g.setPaint(current.seriesColor());
            g.fill(HIGHLIGHT_SHAPE);
            g.setPaint(JBColor.foreground());
            g.draw(HIGHLIGHT_SHAPE);
        } finally {
            g.dispose();
        }
    }

    private @Nullable HitPoint findClosestHitPoint(@NotNull ChartPanel chartPanel,
                                                   @NotNull Point2D mouseJava2D,
                                                   @NotNull Rectangle2D dataArea) {
        var entityCollection = chartPanel.getChartRenderingInfo().getEntityCollection();
        if (entityCollection == null) return null;

        boolean horizontalDomain = chartPanel.getChart().getPlot() instanceof CategoryPlot p
                && p.getOrientation() == PlotOrientation.HORIZONTAL;

        HitPoint bestMatch = null;
        double minDistance = Double.MAX_VALUE;

        for (Object entity : entityCollection.getEntities()) {
            if (entity instanceof ChartEntity chartEntity) {
                HitPoint candidate = toHitPoint(chartPanel, chartEntity, dataArea);
                if (candidate != null) {
                    double delta = horizontalDomain
                            ? Math.abs(candidate.screenPoint().getY() - mouseJava2D.getY())
                            : Math.abs(candidate.screenPoint().getX() - mouseJava2D.getX());
                    if (delta <= HIT_TOLERANCE_PX) {
                        double dist = candidate.screenPoint().distance(mouseJava2D);
                        if (dist < minDistance) {
                            minDistance = dist;
                            bestMatch = candidate;
                        }
                    }
                }
            }
        }
        return bestMatch;
    }

    private static @Nullable HitPoint toHitPoint(@NotNull ChartPanel chartPanel,
                                                 @NotNull ChartEntity entity,
                                                 @NotNull Rectangle2D dataArea) {
        if (entity instanceof XYItemEntity e && chartPanel.getChart().getPlot() instanceof XYPlot plot) {
            XYDataset dataset = e.getDataset();
            int series = e.getSeriesIndex();
            int item = e.getItem();
            double dataY = dataset.getYValue(series, item);
            double sx = plot.getDomainAxis().valueToJava2D(dataset.getXValue(series, item), dataArea, plot.getDomainAxisEdge());
            double sy = plot.getRangeAxis().valueToJava2D(dataY, dataArea, plot.getRangeAxisEdge());
            return new HitPoint(
                    new Point2D.Double(sx, sy),
                    DQLBundle.message("components.visualization.tooltip.value", dataset.getSeriesKey(series), formatValue(dataY)),
                    plot.getRenderer().getSeriesPaint(series)
            );
        }

        if (entity instanceof CategoryItemEntity e && chartPanel.getChart().getPlot() instanceof CategoryPlot plot) {
            CategoryDataset dataset = e.getDataset();
            Number value = dataset.getValue(e.getRowKey(), e.getColumnKey());
            if (value != null) {
                int row = dataset.getRowIndex(e.getRowKey());
                double dataY = value.doubleValue();
                Rectangle2D barBounds = e.getArea().getBounds2D();
                boolean horizontal = plot.getOrientation() == PlotOrientation.HORIZONTAL;
                double sx = horizontal ? plot.getRangeAxis().valueToJava2D(dataY, dataArea, plot.getRangeAxisEdge()) : barBounds.getCenterX();
                double sy = horizontal ? barBounds.getCenterY() : plot.getRangeAxis().valueToJava2D(dataY, dataArea, plot.getRangeAxisEdge());
                return new HitPoint(
                        new Point2D.Double(sx, sy),
                        DQLBundle.message("components.visualization.tooltip.value", e.getRowKey(), formatValue(dataY)),
                        plot.getRenderer().getSeriesPaint(row)
                );
            }
        }
        return null;
    }

    private static String formatValue(double value) {
        if (value == Math.floor(value) && Double.isFinite(value)) {
            return String.valueOf((long) value);
        }
        return String.format("%.4g", value);
    }

    private record HitPoint(@NotNull Point2D screenPoint, @NotNull String label, @Nullable Paint seriesColor) {
    }
}

package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.CategoryItemEntity;
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
import java.util.Collections;

class HoverOverlay extends AbstractOverlay implements Overlay {
    private static final int XY_HIT_TOLERANCE_PX = 20;
    private static final Shape HIGHLIGHT_SHAPE = new Ellipse2D.Double(-5.0, -5.0, 10, 10);
    private static final Stroke HIGHLIGHT_STROKE = new BasicStroke(1.5f);

    private final TooltipPanel tooltipPanel;
    private @Nullable HitPoint current;

    HoverOverlay(@NotNull TooltipPanel tooltipPanel) {
        this.tooltipPanel = tooltipPanel;
    }

    void onMouseMoved(@NotNull ChartPanel chartPanel, @NotNull Point2D java2D) {
        Rectangle2D dataArea = chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea();
        if (!dataArea.contains(java2D)) {
            clear();
            return;
        }

        current = switch (chartPanel.getChart().getPlot()) {
            case CategoryPlot p -> findClosest(chartPanel, java2D, p);
            case XYPlot p -> findClosest(chartPanel, java2D, dataArea, p);
            default -> null;
        };

        if (current != null) {
            showTooltip(chartPanel, current);
        } else {
            tooltipPanel.hideTooltip();
        }
    }

    void clear() {
        current = null;
        tooltipPanel.hideTooltip();
    }

    @Override
    public void paintOverlay(@NotNull Graphics2D g2, @NotNull ChartPanel chartPanel) {
        if (current == null) {
            return;
        }
        Point2D anchor = chartPanel.translateJava2DToScreen(current.screenPoint());
        Graphics2D g = (Graphics2D) g2.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(anchor.getX(), anchor.getY());
            g.setStroke(HIGHLIGHT_STROKE);
            if (current.seriesColor() != null) {
                g.setPaint(current.seriesColor());
                g.fill(HIGHLIGHT_SHAPE);
            }
            g.setPaint(JBColor.foreground());
            g.draw(HIGHLIGHT_SHAPE);
        } finally {
            g.dispose();
        }
    }

    private static @Nullable HitPoint findClosest(@NotNull ChartPanel chartPanel, @NotNull Point2D java2D,
                                                  @NotNull CategoryPlot plot) {
        boolean horizontal = plot.getOrientation() == PlotOrientation.HORIZONTAL;
        CategoryItemEntity best = null;
        double minDelta = Double.MAX_VALUE;
        for (Object obj : entities(chartPanel)) {
            if (!(obj instanceof CategoryItemEntity e)) {
                continue;
            }
            Rectangle2D bar = e.getArea().getBounds2D();
            Point2D toCompare = horizontal ? new Point2D.Double(bar.getCenterX(), java2D.getY()) : new Point2D.Double(java2D.getX(), bar.getCenterY());
            if (!bar.contains(toCompare)) {
                continue;
            }
            double delta = horizontal
                    ? Math.abs(bar.getCenterY() - java2D.getY())
                    : Math.abs(bar.getCenterX() - java2D.getX());
            if (delta < minDelta) {
                minDelta = delta;
                best = e;
            }
        }
        if (best == null) {
            return null;
        }
        CategoryDataset dataset = best.getDataset();
        if (dataset.getRowIndex(best.getRowKey()) < 0 || dataset.getColumnIndex(best.getColumnKey()) < 0) {
            return null;
        }
        Number value = dataset.getValue(best.getRowKey(), best.getColumnKey());
        if (value == null) {
            return null;
        }
        Rectangle2D bar = best.getArea().getBounds2D();
        double sx = horizontal ? bar.getMaxX() : bar.getCenterX();
        double sy = horizontal ? bar.getCenterY() : bar.getMinY();
        return new HitPoint(
                new Point2D.Double(sx, sy),
                DQLBundle.message("components.visualization.tooltip.value", best.getRowKey(), formatValue(value.doubleValue())),
                plot.getRenderer().getSeriesPaint(dataset.getRowIndex(best.getRowKey()))
        );
    }

    private static @Nullable HitPoint findClosest(@NotNull ChartPanel chartPanel, @NotNull Point2D java2D,
                                                  @NotNull Rectangle2D dataArea, @NotNull XYPlot plot) {
        boolean horizontal = plot.getOrientation() == PlotOrientation.HORIZONTAL;
        HitPoint best = null;
        double minDist = Double.MAX_VALUE;
        for (Object obj : entities(chartPanel)) {
            if (!(obj instanceof XYItemEntity e)) {
                continue;
            }
            XYDataset dataset = e.getDataset();
            int series = e.getSeriesIndex();
            int item = e.getItem();
            double dataY = dataset.getYValue(series, item);
            double sx = plot.getDomainAxis().valueToJava2D(dataset.getXValue(series, item), dataArea, plot.getDomainAxisEdge());
            double sy = plot.getRangeAxis().valueToJava2D(dataY, dataArea, plot.getRangeAxisEdge());
            double axisDelta = horizontal ? Math.abs(sy - java2D.getY()) : Math.abs(sx - java2D.getX());
            if (axisDelta > XY_HIT_TOLERANCE_PX) {
                continue;
            }
            double dist = Math.hypot(sx - java2D.getX(), sy - java2D.getY());
            if (dist < minDist) {
                minDist = dist;
                best = new HitPoint(
                        new Point2D.Double(sx, sy),
                        DQLBundle.message("components.visualization.tooltip.value", dataset.getSeriesKey(series), formatValue(dataY)),
                        plot.getRenderer().getSeriesPaint(series)
                );
            }
        }
        return best;
    }

    private void showTooltip(@NotNull ChartPanel chartPanel, @NotNull HitPoint hit) {
        JRootPane rootPane = SwingUtilities.getRootPane(chartPanel);
        if (rootPane == null) {
            return;
        }
        Point2D anchor = chartPanel.translateJava2DToScreen(hit.screenPoint());
        Point converted = SwingUtilities.convertPoint(chartPanel,
                (int) anchor.getX(), (int) anchor.getY(), rootPane.getLayeredPane());
        tooltipPanel.showTooltip(hit.label(), hit.seriesColor(),
                converted.x, converted.y, rootPane.getLayeredPane().getWidth());
    }

    private static @NotNull Iterable<?> entities(@NotNull ChartPanel chartPanel) {
        var collection = chartPanel.getChartRenderingInfo().getEntityCollection();
        return collection != null ? collection.getEntities() : Collections.emptyList();
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

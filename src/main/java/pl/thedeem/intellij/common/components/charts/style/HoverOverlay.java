package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.panel.AbstractOverlay;
import org.jfree.chart.panel.Overlay;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;

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
        Point2D anchor = chartPanel.translateJava2DToScreen(current.point());
        Graphics2D g = (Graphics2D) g2.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.translate(anchor.getX(), anchor.getY());
            g.setStroke(HIGHLIGHT_STROKE);
            g.setPaint(current.seriesPaint());
            g.fill(HIGHLIGHT_SHAPE);
            g.setPaint(JBColor.foreground());
            g.draw(HIGHLIGHT_SHAPE);
        } finally {
            g.dispose();
        }
    }

    private static @Nullable HitPoint findClosest(
            @NotNull ChartPanel chartPanel,
            @NotNull Point2D java2D,
            @NotNull CategoryPlot plot
    ) {
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
                plot,
                dataset,
                dataset.getRowIndex(best.getRowKey()),
                dataset.getColumnIndex(best.getColumnKey()),
                new Point2D.Double(sx, sy)
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
                best = new HitPoint(plot, dataset, series, item, new Point2D.Double(sx, sy));
            }
        }
        return best;
    }

    private void showTooltip(@NotNull ChartPanel chartPanel, @NotNull HitPoint hit) {
        JRootPane rootPane = SwingUtilities.getRootPane(chartPanel);
        if (rootPane == null) {
            return;
        }
        Point2D anchor = chartPanel.translateJava2DToScreen(hit.point());
        Point converted = SwingUtilities.convertPoint(chartPanel,
                (int) anchor.getX(), (int) anchor.getY(), rootPane.getLayeredPane());
        tooltipPanel.showTooltip(hit, converted, rootPane.getLayeredPane().getWidth());
    }

    private static @NotNull Iterable<?> entities(@NotNull ChartPanel chartPanel) {
        var collection = chartPanel.getChartRenderingInfo().getEntityCollection();
        return collection != null ? collection.getEntities() : Collections.emptyList();
    }

    record HitPoint(
            @NotNull Plot plot,
            @NotNull Dataset dataset,
            int series,
            int item,
            @NotNull Point2D point
    ) {

        @NotNull Paint seriesPaint() {
            Paint paint = null;
            if (plot instanceof XYPlot xyPlot) {
                paint = xyPlot.getRenderer().getSeriesPaint(series);
            } else if (plot instanceof CategoryPlot categoryPlot) {
                paint = categoryPlot.getRenderer().getSeriesPaint(series);
            }
            return paint != null ? paint : JBColor.GRAY;
        }

        double getYValue() {
            return switch (dataset) {
                case XYDataset d -> d.getYValue(series, item);
                case CategoryDataset d -> {
                    Number value = d.getValue(series, item);
                    yield value != null ? value.doubleValue() : Double.NaN;
                }
                default -> Double.NaN;
            };
        }

        @NotNull String getSeriesName() {
            return switch (dataset) {
                case XYDataset d -> d.getSeriesKey(series).toString();
                case CategoryDataset d -> d.getRowKey(series).toString();
                default -> String.valueOf(series);
            };
        }

        @NotNull String getDomainLabel() {
            if (plot instanceof XYPlot xyPlot && dataset instanceof XYDataset xyDataset) {
                double xValue = xyDataset.getXValue(series, item);
                TickUnit tickUnit = switch (xyPlot.getDomainAxis()) {
                    case DateAxis axis -> axis.getTickUnit();
                    case NumberAxis axis -> axis.getTickUnit();
                    default -> null;
                };
                return tickUnit != null ? tickUnit.valueToString(xValue) : String.valueOf(xValue);
            }
            if (plot instanceof CategoryPlot && dataset instanceof CategoryDataset categoryDataset) {
                return categoryDataset.getColumnKey(item).toString();
            }
            return String.valueOf(item);
        }
    }
}

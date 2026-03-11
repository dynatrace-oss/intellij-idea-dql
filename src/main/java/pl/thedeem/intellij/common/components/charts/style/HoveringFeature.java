package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYDataset;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;

class HoveringFeature {
    private static final int XY_HIT_TOLERANCE_PX = JBUI.scale(20);

    @Nullable ChartHitPoint findHitPoint(@NotNull ChartPanel chartPanel, @NotNull Point2D java2D) {
        return switch (chartPanel.getChart().getPlot()) {
            case CategoryPlot p -> findClosest(chartPanel, java2D, p);
            case XYPlot p -> findClosest(chartPanel, java2D,
                    chartPanel.getChartRenderingInfo().getPlotInfo().getDataArea(), p);
            default -> null;
        };
    }

    private static @Nullable ChartHitPoint findClosest(
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
        return new ChartHitPoint(
                plot,
                dataset,
                dataset.getRowIndex(best.getRowKey()),
                dataset.getColumnIndex(best.getColumnKey()),
                new Point2D.Double(sx, sy)
        );
    }

    private static @Nullable ChartHitPoint findClosest(@NotNull ChartPanel chartPanel, @NotNull Point2D java2D,
                                                       @NotNull Rectangle2D dataArea, @NotNull XYPlot plot) {
        boolean horizontal = plot.getOrientation() == PlotOrientation.HORIZONTAL;
        ChartHitPoint best = null;
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
                best = new ChartHitPoint(plot, dataset, series, item, new Point2D.Double(sx, sy));
            }
        }
        return best;
    }

    private static @NotNull Iterable<?> entities(@NotNull ChartPanel chartPanel) {
        var collection = chartPanel.getChartRenderingInfo().getEntityCollection();
        return collection != null ? collection.getEntities() : Collections.emptyList();
    }
}

package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.Dataset;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;

record ChartHitPoint(
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
            return switch (xyPlot.getDomainAxis()) {
                case DateAxis axis -> {
                    DateFormat fmt = axis.getDateFormatOverride();
                    yield fmt != null
                            ? fmt.format(new Date((long) xValue))
                            : axis.getTickUnit().valueToString(xValue);
                }
                case NumberAxis axis -> {
                    NumberFormat fmt = axis.getNumberFormatOverride();
                    yield fmt != null ? fmt.format(xValue) : axis.getTickUnit().valueToString(xValue);
                }
                default -> String.valueOf(xValue);
            };
        }
        if (plot instanceof CategoryPlot && dataset instanceof CategoryDataset categoryDataset) {
            return categoryDataset.getColumnKey(item).toString();
        }
        return String.valueOf(item);
    }
}

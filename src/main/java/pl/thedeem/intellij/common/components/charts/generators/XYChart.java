package pl.thedeem.intellij.common.components.charts.generators;

import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import pl.thedeem.intellij.common.components.charts.ChartSettings;
import pl.thedeem.intellij.common.components.simple.MultipleValuesSelector;
import pl.thedeem.intellij.common.components.simple.SearchableComboBox;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class XYChart extends AbstractChartGenerator {
    public XYChart(@NotNull List<ChartDataRecord> data, @NotNull Map<String, Class<?>> columns) {
        super(data, columns);
    }

    @Override
    public @NotNull JFreeChart createJFreeChart(@NotNull ChartSettings settings) {
        String xCol = settings.get(ChartSettings.SELECTED_SERIES);
        String yLab = generateYLabel(settings);

        if (Instant.class.isAssignableFrom(columns.getOrDefault(xCol, Object.class))) {
            TimeSeriesCollection dataset = createTimeseriesDataset(settings);
            return ChartFactory.createTimeSeriesChart(null, xCol, yLab, dataset, false, true, false);
        }
        XYDataset dataset = createXYDataset(settings);
        return ChartFactory.createXYLineChart(null, xCol, yLab, dataset, PlotOrientation.VERTICAL, false, true, false);
    }

    @Override
    public @Nullable JComponent createSettingsComponent(@NotNull ChartSettings settings, @NotNull Consumer<Boolean> onChange) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Set<String> numCols = columns.entrySet().stream()
                .filter(e -> SUPPORTED_NUMERIC_COLUMNS.test(e.getValue()))
                .map(Map.Entry::getKey).collect(java.util.stream.Collectors.toSet());

        panel.add(LabeledComponent.create(
                new SearchableComboBox<>(numCols, settings.get(ChartSettings.SELECTED_SERIES), EMPTY_COMBO_OPTION, val -> {
                    settings.set(ChartSettings.SELECTED_SERIES, val);
                    onChange.consume(true);
                }),
                DQLBundle.message("components.visualization.settings.xAxis"),
                BorderLayout.NORTH
        ));

        panel.add(LabeledComponent.create(
                new MultipleValuesSelector<>(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, numCols, settings.get(ChartSettings.SELECTED_VALUES, new HashSet<>()), (val, sel) -> {
                    Set<String> values = settings.get(ChartSettings.SELECTED_VALUES, new HashSet<>());
                    if (sel) values.add(val);
                    else values.remove(val);
                    onChange.consume(true);
                }),
                DQLBundle.message("components.visualization.settings.yAxis"),
                BorderLayout.NORTH
        ));

        panel.add(LabeledComponent.create(
                new SearchableComboBox<>(columns.keySet(), settings.get(ChartSettings.GROUP_BY_COLUMN), EMPTY_COMBO_OPTION, val -> {
                    settings.set(ChartSettings.GROUP_BY_COLUMN, val);
                    onChange.consume(true);
                }),
                DQLBundle.message("components.visualization.settings.groupBy"),
                BorderLayout.NORTH
        ));

        return panel;
    }

    @Override
    public boolean isReady(@NotNull ChartSettings settings) {
        return settings.get(ChartSettings.SELECTED_SERIES) != null &&
                !settings.get(ChartSettings.SELECTED_VALUES, Set.of()).isEmpty();
    }

    @Override
    protected void configureChart(@NotNull JFreeChart chart, @NotNull ChartSettings settings) {
        super.configureChart(chart, settings);

        if (chart.getPlot() instanceof XYPlot plot && plot.getRenderer() instanceof XYLineAndShapeRenderer renderer) {
            renderer.setDefaultShapesVisible(true);
        }
    }

    private @NotNull TimeSeriesCollection createTimeseriesDataset(@NotNull ChartSettings settings) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        String xCol = settings.get(ChartSettings.SELECTED_SERIES, "");
        Set<String> yCols = settings.get(ChartSettings.SELECTED_VALUES, Set.of());
        String groupCol = settings.get(ChartSettings.GROUP_BY_COLUMN);
        Map<String, TimeSeries> seriesMap = new LinkedHashMap<>();
        for (ChartDataRecord record : data) {
            String group = groupCol != null ? Objects.requireNonNullElseGet(
                    record.valueOf(groupCol),
                    () -> DQLBundle.message("components.visualization.information.emptyDataPoint")
            ).toString() : null;

            List<Number> xValues = record.numericValueOf(xCol);
            for (String yCol : yCols) {
                List<Number> yValues = record.numericValueOf(yCol);
                for (int i = 0; i < xValues.size(); i++) {
                    if (xValues.get(i) != null && yValues.size() > i && yValues.get(i) != null) {
                        String key = yCol;
                        if (groupCol != null) {
                            key = group + (yCols.size() > 1 ? " - " + key : "");
                        }
                        seriesMap.computeIfAbsent(key, TimeSeries::new)
                                .addOrUpdate(new Millisecond(Date.from(Instant.ofEpochMilli(xValues.get(i).longValue()))), yValues.get(i));
                    }
                }
            }
        }
        seriesMap.values().forEach(dataset::addSeries);
        return dataset;
    }

    private @NotNull XYDataset createXYDataset(@NotNull ChartSettings settings) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        String xCol = settings.get(ChartSettings.SELECTED_SERIES, "");
        Set<String> yCols = settings.get(ChartSettings.SELECTED_VALUES, Set.of());
        String groupCol = settings.get(ChartSettings.GROUP_BY_COLUMN);
        Map<String, XYSeries> seriesMap = new LinkedHashMap<>();
        for (ChartDataRecord record : data) {
            String group = groupCol != null ? Objects.requireNonNullElseGet(
                    record.valueOf(groupCol),
                    () -> DQLBundle.message("components.visualization.information.emptyDataPoint")
            ).toString() : null;
            List<Number> xValues = record.numericValueOf(xCol);
            for (String yCol : yCols) {
                List<Number> yValues = record.numericValueOf(yCol);
                for (int i = 0; i < xValues.size(); i++) {
                    if (xValues.get(i) != null && yValues.size() > i && yValues.get(i) != null) {
                        String key = yCol;
                        if (groupCol != null) {
                            key = group + (yCols.size() > 1 ? " - " + key : "");
                        }
                        seriesMap.computeIfAbsent(key, XYSeries::new).add(xValues.get(i), yValues.get(i));
                    }
                }
            }
        }
        seriesMap.values().forEach(dataset::addSeries);
        return dataset;
    }
}
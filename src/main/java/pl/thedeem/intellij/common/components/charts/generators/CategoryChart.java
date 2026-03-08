package pl.thedeem.intellij.common.components.charts.generators;

import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.SlidingCategoryDataset;
import pl.thedeem.intellij.common.components.charts.ChartSettings;
import pl.thedeem.intellij.common.components.simple.GroupedSettingsComponent;
import pl.thedeem.intellij.common.components.simple.MultipleValuesSelector;
import pl.thedeem.intellij.common.components.simple.SearchableComboBox;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryChart extends AbstractChartGenerator {
    public CategoryChart(@NotNull List<ChartDataRecord> data, @NotNull Map<String, Class<?>> columns) {
        super(data, columns);
    }

    @Override
    public @NotNull JFreeChart createJFreeChart(@NotNull ChartSettings chartSettings) {
        return ChartFactory.createBarChart(
                null,
                chartSettings.get(ChartSettings.SELECTED_SERIES),
                generateYLabel(chartSettings),
                createDataset(chartSettings),
                chartSettings.get(ChartSettings.CHART_ORIENTATION, PlotOrientation.VERTICAL),
                false,
                true,
                false
        );
    }

    @Override
    protected void configureChart(@NotNull JFreeChart chart, @NotNull ChartSettings settings) {
        CategoryPlot plot = chart.getCategoryPlot();
        if (Boolean.TRUE.equals(settings.get(ChartSettings.LOG_SCALE))) {
            plot.setRangeAxis(new LogAxis(plot.getRangeAxis().getLabel()));
        }
        super.configureChart(chart, settings);
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setMaximumCategoryLabelWidthRatio(2.0f);
        if (settings.get(ChartSettings.CHART_ORIENTATION, PlotOrientation.VERTICAL) == PlotOrientation.VERTICAL) {
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        } else {
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
        }
    }

    @Override
    public @Nullable JComponent createSettingsComponent(
            @NotNull ChartSettings settings,
            @NotNull Consumer<Boolean> onChange
    ) {
        JComponent panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new GroupedSettingsComponent(DQLBundle.message("components.visualization.settings.groups.series"))
                .addSetting(LabeledComponent.create(
                        new SearchableComboBox<>(
                                columns.keySet(),
                                settings.get(ChartSettings.SELECTED_SERIES),
                                EMPTY_COMBO_OPTION,
                                selected -> {
                                    if (!Objects.equals(settings.get(ChartSettings.SELECTED_SERIES), selected)) {
                                        settings.set(ChartSettings.SELECTED_SERIES, selected);
                                        onChange.consume(true);
                                    }
                                }),
                        DQLBundle.message("components.visualization.settings.xAxis"),
                        BorderLayout.NORTH
                ))
                .addSetting(LabeledComponent.create(
                        new MultipleValuesSelector<>(
                                ListSelectionModel.MULTIPLE_INTERVAL_SELECTION,
                                columns.keySet().stream()
                                        .filter(c -> SUPPORTED_NUMERIC_COLUMNS.test(columns.get(c)))
                                        .collect(Collectors.toSet()),
                                settings.get(ChartSettings.SELECTED_VALUES, new HashSet<>()),
                                (value, selected) -> {
                                    Set<String> values = settings.get(ChartSettings.SELECTED_VALUES, new HashSet<>());
                                    if (selected) {
                                        values.add(value);
                                    } else {
                                        values.remove(value);
                                    }
                                    onChange.consume(true);
                                }),
                        DQLBundle.message("components.visualization.settings.yAxis"),
                        BorderLayout.NORTH
                ))
                .addSetting(LabeledComponent.create(
                        new SearchableComboBox<>(
                                columns.keySet(),
                                settings.get(ChartSettings.GROUP_BY_COLUMN),
                                EMPTY_COMBO_OPTION,
                                selected -> {
                                    if (!Objects.equals(settings.get(ChartSettings.GROUP_BY_COLUMN), selected)) {
                                        settings.set(ChartSettings.GROUP_BY_COLUMN, selected);
                                        onChange.consume(true);
                                    }
                                }),
                        DQLBundle.message("components.visualization.settings.groupBy"),
                        BorderLayout.NORTH
                ))
        );

        JCheckBox logScaleCheckBox = new JCheckBox(
                DQLBundle.message("components.visualization.settings.logScale"),
                Boolean.TRUE.equals(settings.get(ChartSettings.LOG_SCALE))
        );
        logScaleCheckBox.addActionListener(e -> {
            settings.set(ChartSettings.LOG_SCALE, logScaleCheckBox.isSelected());
            onChange.consume(true);
        });
        panel.add(new GroupedSettingsComponent(DQLBundle.message("components.visualization.settings.groups.chartSettings"))
                .addSetting(LabeledComponent.create(
                        new SearchableComboBox<>(
                                Set.of(PlotOrientation.VERTICAL, PlotOrientation.HORIZONTAL),
                                settings.get(ChartSettings.CHART_ORIENTATION, PlotOrientation.VERTICAL),
                                null,
                                selected -> {
                                    if (!Objects.equals(settings.get(ChartSettings.CHART_ORIENTATION), selected)) {
                                        settings.set(ChartSettings.CHART_ORIENTATION, selected);
                                        onChange.consume(true);
                                    }
                                }) {{
                            setRenderer(SimpleListCellRenderer.create("", position -> {
                                if (position == PlotOrientation.HORIZONTAL) {
                                    return DQLBundle.message("components.visualization.settings.orientation.horizontal");
                                }
                                return DQLBundle.message("components.visualization.settings.orientation.vertical");
                            }));
                        }},
                        DQLBundle.message("components.visualization.settings.orientation"),
                        BorderLayout.NORTH
                ))
                .addSetting(LabeledComponent.create(
                        logScaleCheckBox,
                        DQLBundle.message("components.visualization.settings.scale"),
                        BorderLayout.NORTH
                ))
        );

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    @Override
    public boolean isReady(@NotNull ChartSettings chartSettings) {
        return chartSettings.get(ChartSettings.SELECTED_SERIES) != null
                && !chartSettings.get(ChartSettings.SELECTED_VALUES, new HashSet<>()).isEmpty();
    }

    protected @NotNull CategoryDataset createDataset(@NotNull ChartSettings settings) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String xCol = settings.get(ChartSettings.SELECTED_SERIES);
        Set<String> yCols = settings.get(ChartSettings.SELECTED_VALUES, Set.of());
        String groupCol = settings.get(ChartSettings.GROUP_BY_COLUMN);

        if (xCol == null || yCols.isEmpty()) {
            return dataset;
        }

        for (ChartDataRecord record : data) {
            List<String> categories = record.labelsOf(xCol);
            if (categories.isEmpty()) {
                continue;
            }
            for (String yCol : yCols) {
                List<Number> values = record.numericValueOf(yCol);
                for (int i = 0; i < values.size(); i++) {
                    Number value = values.get(i);
                    if (value != null) {
                        String seriesKey = (groupCol == null) ? yCol :
                                Objects.requireNonNullElseGet(
                                        record.valueOf(groupCol),
                                        () -> DQLBundle.message("components.visualization.information.emptyDataPoint")
                                ).toString();
                        String categoryKey = categories.size() > i ? categories.get(i) : categories.getLast();
                        if (groupCol != null && yCols.size() > 1) {
                            seriesKey += " (" + String.join(", ", record.labelsOf(yCol)) + ")";
                        }
                        dataset.addValue(value, seriesKey, categoryKey);
                    }
                }
            }
        }
        return new SlidingCategoryDataset(dataset, 0, dataset.getColumnCount());
    }
}

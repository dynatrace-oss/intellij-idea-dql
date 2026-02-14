package pl.thedeem.intellij.common.components.charts.generators;

import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import pl.thedeem.intellij.common.components.charts.ChartSettings;
import pl.thedeem.intellij.common.components.simple.SearchableComboBox;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PieChart extends AbstractChartGenerator {
    public PieChart(@NotNull List<ChartDataRecord> data, @NotNull Map<String, Class<?>> columns) {
        super(data, columns);
    }

    @Override
    public @NotNull JFreeChart createJFreeChart(@NotNull ChartSettings settings) {
        return ChartFactory.createPieChart(null, createDataset(settings), false, true, false);
    }

    @Override
    public @Nullable JComponent createSettingsComponent(
            @NotNull ChartSettings chartSettings,
            @NotNull Consumer<Boolean> settingsChangeListener
    ) {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));

        settingsPanel.add(LabeledComponent.create(
                new SearchableComboBox<>(
                        columns.keySet(),
                        chartSettings.get(ChartSettings.SELECTED_SERIES),
                        EMPTY_COMBO_OPTION,
                        selected -> {
                            if (!Objects.equals(selected, chartSettings.get(ChartSettings.SELECTED_SERIES))) {
                                chartSettings.set(ChartSettings.SELECTED_SERIES, selected);
                                settingsChangeListener.consume(true);
                            }
                        }),
                DQLBundle.message("components.visualization.settings.pieChart.category"),
                BorderLayout.WEST
        ));

        settingsPanel.add(LabeledComponent.create(
                new SearchableComboBox<>(
                        columns.keySet().stream()
                                .filter(c -> SUPPORTED_NUMERIC_COLUMNS.test(columns.get(c)))
                                .collect(Collectors.toSet()),
                        chartSettings.get(ChartSettings.SELECTED_VALUES),
                        EMPTY_COMBO_OPTION,
                        selected -> {
                            if (!Objects.equals(selected, chartSettings.get(ChartSettings.SELECTED_SERIES))) {
                                chartSettings.set(ChartSettings.SELECTED_VALUES, new HashSet<>(Set.of((String) selected)));
                                settingsChangeListener.consume(true);
                            }
                        }),
                DQLBundle.message("components.visualization.settings.pieChart.value"),
                BorderLayout.WEST
        ));

        JBCheckBox displayLabelsCheckbox = new JBCheckBox(
                DQLBundle.message("components.visualization.settings.pieChart.displayLabels.enabled"),
                chartSettings.get(ChartSettings.PIE_DISPLAY_LABELS, true)
        );
        displayLabelsCheckbox.addActionListener(e -> {
            chartSettings.set(ChartSettings.PIE_DISPLAY_LABELS, displayLabelsCheckbox.isSelected());
            settingsChangeListener.consume(true);
        });

        settingsPanel.add(LabeledComponent.create(
                displayLabelsCheckbox,
                DQLBundle.message("components.visualization.settings.pieChart.displayLabels"),
                BorderLayout.WEST
        ));

        settingsPanel.add(Box.createVerticalGlue());
        return settingsPanel;
    }

    @Override
    public boolean isReady(@NotNull ChartSettings chartSettings) {
        return chartSettings.get(ChartSettings.SELECTED_SERIES) != null
                && !chartSettings.get(ChartSettings.SELECTED_VALUES, new HashSet<>()).isEmpty();
    }

    @Override
    protected void configureChart(@NotNull JFreeChart chart, @NotNull ChartSettings settings) {
        super.configureChart(chart, settings);
        if (chart.getPlot() instanceof PiePlot<?> plot && !settings.get(ChartSettings.PIE_DISPLAY_LABELS, true)) {
            plot.setLabelGenerator(null);
        }
    }

    protected @NotNull DefaultPieDataset<String> createDataset(@NotNull ChartSettings settings) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        String xCol = settings.get(ChartSettings.SELECTED_SERIES);
        String yCol = settings.get(ChartSettings.SELECTED_VALUES, Set.of()).stream().findFirst().orElse(null);

        if (xCol == null || yCol == null) return dataset;

        Map<String, Double> sums = new LinkedHashMap<>();
        for (ChartDataRecord record : data) {
            List<String> categories = record.labelsOf(xCol);
            List<Number> values = record.numericValueOf(yCol);
            for (int i = 0; i < values.size(); i++) {
                Number value = values.get(i);
                if (value != null) {
                    String categoryKey = categories.size() > i ? categories.get(i) : categories.getLast();
                    sums.merge(categoryKey, value.doubleValue(), Double::sum);
                }
            }
        }

        sums.forEach((key, sum) -> {
            if (sum > 0) dataset.setValue(key, sum);
        });

        return dataset;
    }
}

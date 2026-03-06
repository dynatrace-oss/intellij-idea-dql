package pl.thedeem.intellij.common.components.charts.generators;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import pl.thedeem.intellij.common.components.charts.ChartSettings;
import pl.thedeem.intellij.common.components.charts.style.IntelliJChartTheme;
import pl.thedeem.intellij.dql.DQLBundle;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public abstract class AbstractChartGenerator implements ChartGenerator {
    protected static final String EMPTY_COMBO_OPTION = "-";
    protected static final Predicate<Class<?>> SUPPORTED_NUMERIC_COLUMNS = c ->
            c != null && (Number.class.isAssignableFrom(c) || Instant.class.isAssignableFrom(c) || List.class.isAssignableFrom(c));

    private static final ChartTheme INTELLIJ_CHART_THEME = new IntelliJChartTheme();

    protected final List<ChartDataRecord> data;
    protected final @NotNull Map<String, Class<?>> columns;

    protected AbstractChartGenerator(@NotNull List<ChartDataRecord> data, @NotNull Map<String, Class<?>> columns) {
        this.data = data;
        this.columns = columns;
    }

    @Override
    public @NotNull JFreeChart createChart(@NotNull ChartSettings settings) {
        JFreeChart chart = createJFreeChart(settings);
        configureChart(chart, settings);
        return chart;
    }

    protected abstract @NotNull JFreeChart createJFreeChart(@NotNull ChartSettings settings);

    protected void configureChart(@NotNull JFreeChart chart, @NotNull ChartSettings settings) {
        INTELLIJ_CHART_THEME.apply(chart);
    }

    protected @NotNull String generateYLabel(@NotNull ChartSettings settings) {
        Set<String> values = settings.get(ChartSettings.SELECTED_VALUES);
        if (values == null || values.isEmpty()) {
            return DQLBundle.message("components.visualization.axis.values.unknown");
        }
        return values.size() == 1 ? values.iterator().next() : DQLBundle.print(values);
    }
}
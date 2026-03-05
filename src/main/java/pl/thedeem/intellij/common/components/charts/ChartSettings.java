package pl.thedeem.intellij.common.components.charts;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.plot.PlotOrientation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ChartSettings {
    public static final Setting<LegendPosition> LEGEND_POSITION = new Setting<>("legendPosition", LegendPosition.class);
    public static final Setting<String> SELECTED_SERIES = new Setting<>("selectedSeriesColumn", String.class);
    public static final Setting<String> GROUP_BY_COLUMN = new Setting<>("selectedGroupByColumn", String.class);
    public static final Setting<PlotOrientation> CHART_ORIENTATION = new Setting<>("chartOrientation", PlotOrientation.class);
    public static final Setting<Boolean> PIE_DISPLAY_LABELS = new Setting<>("displayLabels", Boolean.class);
    @SuppressWarnings("unchecked")
    public static final Setting<Set<String>> SELECTED_VALUES = new Setting<>("selectedValueColumns", (Class<Set<String>>) (Class<?>) Set.class);
    private final Map<String, Object> chartSettings;

    public ChartSettings() {
        this(Map.of());
    }

    public ChartSettings(@NotNull Map<String, Object> initial) {
        this.chartSettings = new HashMap<>(initial);
    }

    public <T> @Nullable T get(@NotNull Setting<T> setting) {
        return get(setting.key(), setting.type());
    }

    public <T> @NotNull T get(@NotNull Setting<T> setting, @NotNull T defaultValue) {
        return Objects.requireNonNullElse(get(setting.key(), setting.type()), defaultValue);
    }

    public <T> @Nullable T get(@NotNull String key, @NotNull Class<T> type) {
        Object value = chartSettings.get(key);
        if (type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }

    public <T> ChartSettings set(@NotNull Setting<T> setting, @Nullable T value) {
        chartSettings.put(setting.key(), value);
        return this;
    }

    public record Setting<T>(@NotNull String key, @NotNull Class<T> type) {
    }

    public enum LegendPosition {
        NONE,
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }
}

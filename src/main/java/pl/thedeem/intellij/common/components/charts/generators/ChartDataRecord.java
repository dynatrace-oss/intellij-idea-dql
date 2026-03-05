package pl.thedeem.intellij.common.components.charts.generators;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ChartDataRecord {
    @Nullable Object valueOf(@NotNull String column);

    @NotNull List<Number> numericValueOf(@NotNull String column);

    @NotNull List<String> labelsOf(@NotNull String column);
}

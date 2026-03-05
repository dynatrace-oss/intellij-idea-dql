package pl.thedeem.intellij.common.components.charts.generators;

import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.JFreeChart;
import pl.thedeem.intellij.common.components.charts.ChartSettings;

import javax.swing.*;

public interface ChartGenerator {
    @NotNull JFreeChart createChart(@NotNull ChartSettings chartSettings);

    @Nullable JComponent createSettingsComponent(
            @NotNull ChartSettings chartSettings,
            @NotNull Consumer<Boolean> settingsChangeListener
    );

    boolean isReady(@NotNull ChartSettings chartSettings);
}

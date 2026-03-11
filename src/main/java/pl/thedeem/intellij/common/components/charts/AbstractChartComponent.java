package pl.thedeem.intellij.common.components.charts;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.Nullable;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.components.charts.generators.ChartGenerator;
import pl.thedeem.intellij.common.components.charts.style.DTStyleChartPanel;
import pl.thedeem.intellij.common.components.simple.SearchableComboBox;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractChartComponent extends BorderLayoutPanel {
    private static final Logger logger = Logger.getInstance(AbstractChartComponent.class);
    protected final BorderLayoutPanel chartContainer = new BorderLayoutPanel();
    protected final BorderLayoutPanel legendPanel = new BorderLayoutPanel();
    protected final JPanel settingsPanel = new JPanel(new MigLayout("fillx, wrap 1"));
    protected final OnePixelSplitter contentPanel = new OnePixelSplitter();
    protected ChartGenerator chartGenerator;
    protected final ChartSettings chartSettings;

    public AbstractChartComponent() {
        this.chartSettings = new ChartSettings()
                .set(ChartSettings.LEGEND_POSITION, ChartSettings.LegendPosition.RIGHT)
                .set(ChartSettings.SELECTED_VALUES, new HashSet<>());
        this.legendPanel.setOpaque(false);
        this.legendPanel.setBorder(JBUI.Borders.empty(JBUI.scale(5)));
        this.contentPanel.setOpaque(false);
        this.contentPanel.setBorder(JBUI.Borders.empty());
        this.settingsPanel.setBorder(JBUI.Borders.empty(JBUI.scale(5)));
        setOpaque(false);
        setBorder(JBUI.Borders.empty());

        addToCenter(new OnePixelSplitter() {{
            setFirstComponent(contentPanel);
            setSecondComponent(new TransparentScrollPane(settingsPanel));
            setProportion(0.85f);
        }});
    }

    public abstract boolean isDataEmpty();

    public void showChart(@Nullable ChartGenerator chartGenerator) {
        this.chartGenerator = chartGenerator;
        refreshSettingsPanel();
        refreshChart();
    }

    protected void refreshContent(@Nullable ChartSettings.LegendPosition legendPosition) {
        TransparentScrollPane scrollableLegend = new TransparentScrollPane(legendPanel);
        BorderLayoutPanel chart = new BorderLayoutPanel().addToCenter(chartContainer);
        switch (legendPosition) {
            case LEFT -> {
                contentPanel.setOrientation(false);
                contentPanel.setFirstComponent(scrollableLegend);
                contentPanel.setSecondComponent(chart);
                contentPanel.setProportion(0.1f);
            }
            case RIGHT -> {
                contentPanel.setOrientation(false);
                contentPanel.setFirstComponent(chart);
                contentPanel.setSecondComponent(scrollableLegend);
                contentPanel.setProportion(0.9f);
            }
            case TOP -> {
                contentPanel.setOrientation(true);
                contentPanel.setFirstComponent(scrollableLegend);
                contentPanel.setSecondComponent(chart);
                contentPanel.setProportion(0.1f);
            }
            case BOTTOM -> {
                contentPanel.setOrientation(true);
                contentPanel.setFirstComponent(chart);
                contentPanel.setSecondComponent(scrollableLegend);
                contentPanel.setProportion(0.9f);
            }
            case null, default -> {
                contentPanel.setProportion(1f);
                contentPanel.setFirstComponent(chart);
                contentPanel.setSecondComponent(null);
            }
        }
        legendPanel.repaint();
        legendPanel.revalidate();
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    protected void refreshChart() {
        legendPanel.removeAll();
        chartContainer.removeAll();

        if (isDataEmpty()) {
            chartContainer.addToCenter(new TransparentScrollPane(new InformationComponent(
                    DQLBundle.message("components.visualization.information.noData"),
                    AllIcons.General.Information
            )));
            chartContainer.revalidate();
            chartContainer.repaint();
            refreshContent(null);
            return;
        }
        if (chartGenerator == null || requiresConfiguration()) {
            chartContainer.addToCenter(new TransparentScrollPane(new InformationComponent(
                    DQLBundle.message("components.visualization.information.notConfigured"),
                    AllIcons.General.Information
            )));
            chartContainer.revalidate();
            chartContainer.repaint();
            refreshContent(null);
            return;
        }
        try {
            JFreeChart chart = chartGenerator.createChart(chartSettings);
            if (chart.getLegend() != null) {
                chart.removeLegend();
            }
            ChartPanel chartPanel = new DTStyleChartPanel(chart);
            chartContainer.addToCenter(chartPanel);
            chartContainer.revalidate();
            chartContainer.repaint();
            ChartSettings.LegendPosition legendPosition = chartSettings.get(ChartSettings.LEGEND_POSITION);
            if (!requiresConfiguration() && legendPosition != ChartSettings.LegendPosition.NONE) {
                ChartLegendPanel customLegend = new ChartLegendPanel(
                        chart,
                        Set.of(ChartSettings.LegendPosition.BOTTOM, ChartSettings.LegendPosition.TOP)
                                .contains(legendPosition)
                );
                legendPanel.addToCenter(customLegend);
                legendPanel.revalidate();
                legendPanel.repaint();
            }
            refreshContent(legendPosition);
        } catch (Exception e) {
            logger.warn("Error when generating chart", e);
            chartContainer.addToCenter(new TransparentScrollPane(new InformationComponent(
                    DQLBundle.message("components.visualization.information.generatorError", e.getMessage()),
                    AllIcons.General.Error
            )));
            chartContainer.revalidate();
            chartContainer.repaint();
        }

        settingsPanel.repaint();
        settingsPanel.revalidate();
    }

    protected boolean requiresConfiguration() {
        return isDataEmpty() || chartGenerator == null || !chartGenerator.isReady(chartSettings);
    }

    protected void refreshSettingsPanel() {
        settingsPanel.removeAll();
        settingsPanel.add(LabeledComponent.create(
                new SearchableComboBox<>(
                        Set.of(ChartSettings.LegendPosition.values()),
                        chartSettings.get(ChartSettings.LEGEND_POSITION),
                        null,
                        selected -> {
                            if (!Objects.equals(chartSettings.get(ChartSettings.LEGEND_POSITION), selected)) {
                                chartSettings.set(ChartSettings.LEGEND_POSITION, selected);
                                refreshChart();
                                refreshContent(selected);
                            }
                        }) {{
                    setRenderer(SimpleListCellRenderer.create("", position -> switch (position) {
                        case ChartSettings.LegendPosition.TOP ->
                                DQLBundle.message("components.visualization.settings.legend.position.top");
                        case ChartSettings.LegendPosition.BOTTOM ->
                                DQLBundle.message("components.visualization.settings.legend.position.bottom");
                        case ChartSettings.LegendPosition.LEFT ->
                                DQLBundle.message("components.visualization.settings.legend.position.left");
                        case ChartSettings.LegendPosition.RIGHT ->
                                DQLBundle.message("components.visualization.settings.legend.position.right");
                        case null, default ->
                                DQLBundle.message("components.visualization.settings.legend.position.none");
                    }));
                }},
                DQLBundle.message("components.visualization.settings.legend.position"),
                BorderLayout.NORTH
        ));
        JComponent customSettings = chartGenerator.createSettingsComponent(chartSettings, (refresh) -> {
            if (refresh) {
                refreshChart();
            }
        });
        if (customSettings != null) {
            settingsPanel.add(customSettings);
        }
        settingsPanel.add(Box.createVerticalGlue());
        settingsPanel.revalidate();
        settingsPanel.repaint();
    }
}

package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.RectangleInsets;

import java.awt.*;

public class IntelliJChartTheme extends StandardChartTheme {
    private static final Color BG = JBColor.background();
    private static final Color FG = JBColor.foreground();
    private static final Color GRID = JBColor.namedColor("Component.borderColor", new JBColor(0xC4C4C4, 0x505050));
    private static final Color PLOT_BG = JBColor.namedColor("Editor.background", BG);

    private static final Paint[] SERIES_COLORS = {
            JBColor.namedColor("Charts.Blue", new JBColor(0x3574F0, 0x548AF7)),
            JBColor.namedColor("Charts.Green", new JBColor(0x59A869, 0x499C54)),
            JBColor.namedColor("Charts.Red", new JBColor(0xE05555, 0xC75450)),
            JBColor.namedColor("Charts.Yellow", new JBColor(0xEDAE49, 0xD9A343)),
            JBColor.namedColor("Charts.Purple", new JBColor(0x9059AF, 0x9F69C7)),
            JBColor.namedColor("Charts.Orange", new JBColor(0xF07C3E, 0xE8743B)),
            JBColor.namedColor("Charts.Cyan", new JBColor(0x3592C4, 0x3592C4)),
            JBColor.namedColor("Charts.Pink", new JBColor(0xE30D99, 0xE30D99)),
            JBColor.namedColor("Charts.Lime", new JBColor(0x68ED09, 0x68ED09))
    };

    public IntelliJChartTheme() {
        super("IntelliJ");

        setChartBackgroundPaint(BG);
        setPlotBackgroundPaint(PLOT_BG);
        setDomainGridlinePaint(GRID);
        setRangeGridlinePaint(GRID);
        setAxisLabelPaint(FG);
        setTickLabelPaint(FG);
        setLabelLinkPaint(FG);
        setItemLabelPaint(FG);
        setGridBandPaint(BG);
        setGridBandAlternatePaint(PLOT_BG);

        setAxisOffset(new RectangleInsets(0, 0, 0, 0));
        setLabelLinkStyle(PieLabelLinkStyle.STANDARD);
        setShadowVisible(false);
        setBarPainter(new StandardBarPainter());
        setPlotOutlinePaint(ColorUtil.toAlpha(GRID, 0));

        setDrawingSupplier(new DefaultDrawingSupplier(
                SERIES_COLORS,
                DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
        ));
        Font label = JBUI.Fonts.label();
        Font bold = label.deriveFont(Font.BOLD, label.getSize() + 1f);
        setRegularFont(label);
        setSmallFont(label);
        setLargeFont(bold);
        setExtraLargeFont(bold);
    }

    @Override
    public void apply(JFreeChart chart) {
        super.apply(chart);
        if (chart.getPlot() instanceof PiePlot<?> pie) {
            pie.setLabelBackgroundPaint(BG);
            pie.setLabelPaint(FG);
            pie.setLabelFont(getRegularFont());
            pie.setLabelOutlinePaint(null);
            pie.setLabelShadowPaint(null);
            pie.setShadowPaint(null);
            pie.setCircular(true);
        }
    }
}
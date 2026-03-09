package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import com.intellij.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Pannable;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class XYPlotInteractionHandler implements PlotInteractionHandler {
    private final XYPlot plot;
    private final Range domainBounds;
    private final Range rangeBounds;

    XYPlotInteractionHandler(@NotNull XYPlot plot) {
        this.plot = plot;
        this.domainBounds = new Range(plot.getDomainAxis().getLowerBound(), plot.getDomainAxis().getUpperBound());
        this.rangeBounds = new Range(plot.getRangeAxis().getLowerBound(), plot.getRangeAxis().getUpperBound());

        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setDomainCrosshairVisible(true);
        plot.setDomainCrosshairLockedOnData(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRangeCrosshairLockedOnData(true);
        plot.setDomainCrosshairPaint(JBColor.GRAY);
        plot.setRangeCrosshairPaint(JBColor.GRAY);
        plot.setDomainCrosshairStroke(CROSSHAIR_STROKE);
        plot.setRangeCrosshairStroke(CROSSHAIR_STROKE);

        plot.getDomainAxis().addChangeListener(event -> {
            if (event.getAxis() instanceof ValueAxis axis) {
                if (axis.getLowerBound() < domainBounds.getLowerBound()) {
                    axis.setLowerBound(domainBounds.getLowerBound());
                }
                if (axis.getUpperBound() > domainBounds.getUpperBound()) {
                    axis.setUpperBound(domainBounds.getUpperBound());
                }
            }
        });
        plot.getRangeAxis().addChangeListener(event -> {
            if (event.getAxis() instanceof ValueAxis axis) {
                if (axis.getLowerBound() < rangeBounds.getLowerBound()) {
                    axis.setLowerBound(rangeBounds.getLowerBound());
                }
                if (axis.getUpperBound() > rangeBounds.getUpperBound()) {
                    axis.setUpperBound(rangeBounds.getUpperBound());
                }
            }
        });
    }

    @Override
    public boolean onMouseMoved(@NotNull Point2D p, @NotNull Rectangle2D dataArea) {
        double mouseX = plot.getDomainAxis().java2DToValue(p.getX(), dataArea, plot.getDomainAxisEdge());
        plot.setDomainCrosshairValue(mouseX, true);
        double mouseY = plot.getRangeAxis().java2DToValue(p.getY(), dataArea, plot.getRangeAxisEdge());
        plot.setRangeCrosshairValue(mouseY, true);
        return true;
    }

    @Override
    public boolean onMouseWheel(@NotNull MouseWheelEvent e, @NotNull ChartPanel panel) {
        if (e.isControlDown()) {
            if (plot instanceof Pannable pannable && pannable.isDomainPannable()) {
                pannable.panDomainAxes(e.getWheelRotation() * 0.05, panel.getChartRenderingInfo().getPlotInfo(), null);
                return false;
            }
        } else if (e.isShiftDown()) {
            if (plot instanceof Pannable pannable && pannable.isRangePannable()) {
                pannable.panRangeAxes(e.getWheelRotation() * -0.1, panel.getChartRenderingInfo().getPlotInfo(), null);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onMiddleMouseDragged(@NotNull Point current, @NotNull Point last, @NotNull ChartPanel panel) {
        Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
        double panX = -(current.getX() - last.getX()) / dataArea.getWidth();
        double panY = (current.getY() - last.getY()) / dataArea.getHeight();

        panX = clampedPan(panX, plot.getDomainAxis(), domainBounds);
        panY = clampedPan(panY, plot.getRangeAxis(), rangeBounds);

        plot.panDomainAxes(panX, panel.getChartRenderingInfo().getPlotInfo(), current);
        plot.panRangeAxes(panY, panel.getChartRenderingInfo().getPlotInfo(), current);
    }

    private static double clampedPan(double pan, @NotNull ValueAxis axis, @NotNull Range bounds) {
        double span = axis.getUpperBound() - axis.getLowerBound();
        double delta = MathUtil.clamp(pan * span,
                bounds.getLowerBound() - axis.getLowerBound(),
                bounds.getUpperBound() - axis.getUpperBound());
        return span != 0 ? delta / span : 0;
    }
}

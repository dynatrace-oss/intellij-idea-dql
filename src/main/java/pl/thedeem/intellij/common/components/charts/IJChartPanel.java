package pl.thedeem.intellij.common.components.charts;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Pannable;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class IJChartPanel extends ChartPanel {
    private final static Stroke CROSSHAIR_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3.0f}, 0);
    private Point lastMousePoint;

    public IJChartPanel(@NotNull JFreeChart chart) {
        super(chart);

        setBorder(JBUI.Borders.empty());
        setBackground(JBColor.background());
        setMouseWheelEnabled(true);
        setDisplayToolTips(true);
        setMouseZoomable(true, true);

        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setReshowDelay(0);

        if (chart.getPlot() instanceof XYPlot plot) {
            setRangeZoomable(true);
            setDomainZoomable(true);
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

            Range domainBounds = new Range(plot.getDomainAxis().getLowerBound(), plot.getDomainAxis().getUpperBound());
            Range rangeBounds = new Range(plot.getRangeAxis().getLowerBound(), plot.getRangeAxis().getUpperBound());

            plot.getDomainAxis().addChangeListener(event -> {
                if (event.getAxis() instanceof ValueAxis axis) {
                    double lower = axis.getLowerBound();
                    double upper = axis.getUpperBound();
                    if (lower < domainBounds.getLowerBound()) {
                        axis.setLowerBound(domainBounds.getLowerBound());
                    }
                    if (upper > domainBounds.getUpperBound()) {
                        axis.setUpperBound(domainBounds.getUpperBound());
                    }
                }
            });
            plot.getRangeAxis().addChangeListener(event -> {
                if (event.getAxis() instanceof ValueAxis axis) {
                    double lower = axis.getLowerBound();
                    double upper = axis.getUpperBound();
                    if (lower < rangeBounds.getLowerBound()) {
                        axis.setLowerBound(rangeBounds.getLowerBound());
                    }
                    if (upper > rangeBounds.getUpperBound()) {
                        axis.setUpperBound(rangeBounds.getUpperBound());
                    }
                }
            });
        }

        if (chart.getPlot() instanceof CategoryPlot plot) {
            setRangeZoomable(true);
            setDomainZoomable(true);
            plot.setRangePannable(true);
            plot.setDomainCrosshairVisible(true);
            plot.setRangeCrosshairVisible(true);
            plot.setRangeCrosshairLockedOnData(true);
            plot.setDomainCrosshairPaint(JBColor.GRAY);
            plot.setRangeCrosshairPaint(JBColor.GRAY);
            plot.setDomainCrosshairStroke(CROSSHAIR_STROKE);
            plot.setRangeCrosshairStroke(CROSSHAIR_STROKE);

            Range rangeBounds = new Range(plot.getRangeAxis().getLowerBound(), plot.getRangeAxis().getUpperBound());
            plot.getRangeAxis().addChangeListener(event -> {
                if (event.getAxis() instanceof ValueAxis axis) {
                    double lower = axis.getLowerBound();
                    double upper = axis.getUpperBound();
                    if (lower < rangeBounds.getLowerBound()) {
                        axis.setLowerBound(rangeBounds.getLowerBound());
                    }
                    if (upper > rangeBounds.getUpperBound()) {
                        axis.setUpperBound(rangeBounds.getUpperBound());
                    }
                }
            });
        }
    }

    @Override
    protected void processMouseMotionEvent(@NotNull MouseEvent e) {
        Rectangle2D dataArea = getChartRenderingInfo().getPlotInfo().getDataArea();
        Point2D p = translateScreenToJava2D(e.getPoint());
        if (getChart().getPlot() instanceof XYPlot plot) {
            if (dataArea.contains(p)) {
                double mouseX = plot.getDomainAxis().java2DToValue(p.getX(), dataArea, plot.getDomainAxisEdge());
                plot.setDomainCrosshairValue(mouseX, true);
                double mouseY = plot.getRangeAxis().java2DToValue(p.getY(), dataArea, plot.getRangeAxisEdge());
                plot.setRangeCrosshairValue(mouseY, true);
                repaint();
            }
        }
        if (getChart().getPlot() instanceof CategoryPlot plot) {
            if (dataArea.contains(p)) {
                double mouseY = plot.getRangeAxis().java2DToValue(p.getY(), dataArea, plot.getRangeAxisEdge());
                plot.setRangeCrosshairValue(mouseY, true);
                repaint();
            }
        }

        if (SwingUtilities.isMiddleMouseButton(e) && lastMousePoint != null) {
            if (getChart().getPlot() instanceof Pannable pannable) {
                if (pannable.isDomainPannable()) {
                    double panPercent = -(e.getX() - lastMousePoint.getX()) / getChartRenderingInfo().getPlotInfo().getDataArea().getWidth();
                    pannable.panDomainAxes(panPercent, getChartRenderingInfo().getPlotInfo(), e.getPoint());
                }
                if (pannable.isRangePannable()) {
                    double panPercent = (e.getY() - lastMousePoint.getY()) / getChartRenderingInfo().getPlotInfo().getDataArea().getHeight();
                    pannable.panRangeAxes(panPercent, getChartRenderingInfo().getPlotInfo(), e.getPoint());
                }
                lastMousePoint = e.getPoint();
            }
        }
        super.processMouseMotionEvent(e);
    }

    @Override
    protected void processMouseWheelEvent(@NotNull MouseWheelEvent e) {
        if (e.isControlDown()) {
            Plot plot = getChart().getPlot();
            if (plot instanceof Pannable pannable && pannable.isDomainPannable()) {
                double panPercent = e.getWheelRotation() * 0.05;
                pannable.panDomainAxes(panPercent, getChartRenderingInfo().getPlotInfo(), e.getPoint());
                return;
            }
        }
        if (e.isShiftDown()) {
            Plot plot = getChart().getPlot();
            if (plot instanceof Pannable pannable && pannable.isRangePannable()) {
                double panPercent = e.getWheelRotation() * -0.1;
                pannable.panRangeAxes(panPercent, getChartRenderingInfo().getPlotInfo(), e.getPoint());
                return;
            }
        }
        super.processMouseWheelEvent(e);
    }

    @Override
    public void mousePressed(@NotNull MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            lastMousePoint = e.getPoint();
        } else {
            super.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(@NotNull MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            lastMousePoint = null;
        } else {
            super.mouseReleased(e);
        }
    }
}
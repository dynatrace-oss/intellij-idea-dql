package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.XYPlot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class DynatraceAlikeChartPanel extends ChartPanel {
    private final TooltipPanel tooltipPanel = new TooltipPanel();
    private final HoverOverlay hover = new HoverOverlay(tooltipPanel);
    private final PlotInteractionHandler handler;
    private Point lastMiddleMousePoint;

    public DynatraceAlikeChartPanel(@NotNull JFreeChart chart) {
        super(chart);

        setBorder(JBUI.Borders.empty());
        setBackground(JBColor.background());
        setMouseWheelEnabled(true);
        setDisplayToolTips(false);
        addOverlay(hover);

        if (chart.getPlot() instanceof XYPlot plot) {
            setMouseZoomable(true, true);
            setRangeZoomable(true);
            setDomainZoomable(true);
            handler = new XYPlotInteractionHandler(plot);
        } else if (chart.getPlot() instanceof CategoryPlot plot) {
            setMouseZoomable(false);
            handler = new CategoryPlotInteractionHandler(plot);
        } else {
            handler = null;
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        JLayeredPane layeredPane = getRootLayeredPane();
        if (layeredPane != null) {
            layeredPane.add(tooltipPanel, JLayeredPane.POPUP_LAYER);
        }
    }

    @Override
    public void removeNotify() {
        JLayeredPane layeredPane = getRootLayeredPane();
        if (layeredPane != null) {
            layeredPane.remove(tooltipPanel);
        }
        hover.clear();
        super.removeNotify();
    }

    @Override
    protected void processMouseMotionEvent(@NotNull MouseEvent e) {
        boolean continueEvent = true;
        if (handler != null) {
            Rectangle2D dataArea = getChartRenderingInfo().getPlotInfo().getDataArea();
            Point2D p = translateScreenToJava2D(e.getPoint());
            if (dataArea.contains(p)) {
                continueEvent = handler.onMouseMoved(p, dataArea);
                hover.onMouseMoved(this, p);
            } else {
                hover.clear();
            }
            if (SwingUtilities.isMiddleMouseButton(e) && lastMiddleMousePoint != null) {
                handler.onMiddleMouseDragged(e.getPoint(), lastMiddleMousePoint, this);
                lastMiddleMousePoint = e.getPoint();
            }
            repaint();
        }
        if (!continueEvent) {
            e.consume();
            return;
        }
        super.processMouseMotionEvent(e);
    }

    @Override
    protected void processMouseWheelEvent(@NotNull MouseWheelEvent e) {
        if (handler != null) {
            boolean continueEvent = handler.onMouseWheel(e, this);
            repaint();
            if (!continueEvent) {
                e.consume();
                return;
            }
        }
        super.processMouseWheelEvent(e);
    }

    @Override
    public void mousePressed(@NotNull MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            lastMiddleMousePoint = e.getPoint();
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } else {
            if (handler != null && SwingUtilities.isLeftMouseButton(e)) {
                handler.onLeftMousePressed(e.getPoint(), this);
            }
            super.mousePressed(e);
        }
    }

    @Override
    public void mouseReleased(@NotNull MouseEvent e) {
        if (SwingUtilities.isMiddleMouseButton(e)) {
            lastMiddleMousePoint = null;
            setCursor(Cursor.getDefaultCursor());
            if (handler != null) {
                handler.onMiddleMouseReleased();
            }
        } else {
            if (handler instanceof CategoryPlotInteractionHandler categoryHandler) {
                categoryHandler.finishSelection(e.getPoint(), this);
                repaint();
            }
            super.mouseReleased(e);
        }
    }

    @Override
    public void paintComponent(@NotNull Graphics g) {
        super.paintComponent(g);
        if (handler != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            handler.paintOverlay(g2, this);
            g2.dispose();
        }
    }

    private JLayeredPane getRootLayeredPane() {
        JRootPane rootPane = SwingUtilities.getRootPane(this);
        return rootPane != null ? rootPane.getLayeredPane() : null;
    }
}
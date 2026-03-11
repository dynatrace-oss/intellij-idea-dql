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

public class DTStyleChartPanel extends ChartPanel {
    private final HoveringFeature hover = new HoveringFeature();
    private final PlotInteractionHandler handler;
    private Point lastMiddleMousePoint;
    private TooltipPanel tooltip;
    private ChartHoverPoint hoverPoint;

    public DTStyleChartPanel(@NotNull JFreeChart chart) {
        super(chart);
        setLayout(null);
        setBorder(JBUI.Borders.empty());
        setBackground(JBColor.background());
        setMouseWheelEnabled(true);
        setDisplayToolTips(false);

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
    protected void processMouseMotionEvent(@NotNull MouseEvent e) {
        boolean continueEvent = true;
        Rectangle2D dataArea = getChartRenderingInfo().getPlotInfo().getDataArea();
        Point2D p = translateScreenToJava2D(e.getPoint());
        if (handler != null) {
            continueEvent = handler.onMouseMoved(p, dataArea);
            if (SwingUtilities.isMiddleMouseButton(e) && lastMiddleMousePoint != null) {
                handler.onMiddleMouseDragged(e.getPoint(), lastMiddleMousePoint, this);
                lastMiddleMousePoint = e.getPoint();
            }
            repaint();
        }
        if (dataArea.contains(p)) {
            ChartHitPoint hit = hover.findHitPoint(this, p);
            if (hit != null) {
                showHit(hit);
            } else {
                hideHit();
            }
        } else {
            hideHit();
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
            try {
                handler.paintOverlay(g2, this);
            } finally {
                g2.dispose();
            }
        }
    }

    private void showHit(@NotNull ChartHitPoint hit) {
        hideHit();
        Point2D anchor = translateJava2DToScreen(hit.point());
        Point anchorPoint = new Point((int) anchor.getX(), (int) anchor.getY());

        tooltip = new TooltipPanel();
        add(tooltip);
        tooltip.showTooltip(hit, anchorPoint, getWidth());

        hoverPoint = new ChartHoverPoint();
        add(hoverPoint);
        hoverPoint.show(anchorPoint, hit.seriesPaint());
    }

    private void hideHit() {
        if (tooltip != null) {
            remove(tooltip);
            tooltip = null;
        }
        if (hoverPoint != null) {
            remove(hoverPoint);
            hoverPoint = null;
        }
        repaint();
    }
}
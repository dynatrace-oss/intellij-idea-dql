package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class CategoryPlotInteractionHandler implements PlotInteractionHandler {
    private static final double ZOOM_FACTOR = 0.15;
    private static final double PAN_FACTOR = 0.1;

    private final CategoryPlot plot;
    private final CategoryDataset fullDataset;
    private int viewStart;
    private int viewSize;

    CategoryPlotInteractionHandler(@NotNull CategoryPlot plot) {
        this.plot = plot;
        this.fullDataset = plot.getDataset();
        this.viewStart = 0;
        this.viewSize = fullDataset.getColumnCount();

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
                if (axis.getLowerBound() < rangeBounds.getLowerBound())
                    axis.setLowerBound(rangeBounds.getLowerBound());
                if (axis.getUpperBound() > rangeBounds.getUpperBound())
                    axis.setUpperBound(rangeBounds.getUpperBound());
            }
        });
    }

    @Override
    public void onMouseMoved(@NotNull Point2D p, @NotNull Rectangle2D dataArea) {
        double mouseY = plot.getRangeAxis().java2DToValue(p.getY(), dataArea, plot.getRangeAxisEdge());
        plot.setRangeCrosshairValue(mouseY, true);
    }

    @Override
    public void onMouseWheel(@NotNull MouseWheelEvent e, @NotNull ChartPanel panel) {
        if (e.isShiftDown()) {
            plot.panRangeAxes(e.getWheelRotation() * -0.1, panel.getChartRenderingInfo().getPlotInfo(), null);
        } else if (e.isControlDown()) {
            panDomain(e.getWheelRotation());
        } else {
            zoomDomain(e.getWheelRotation(), e.getPoint(), panel);
        }
    }

    private void zoomDomain(int wheelRotation, @NotNull Point mousePoint, @NotNull ChartPanel panel) {
        int total = fullDataset.getColumnCount();
        if (total <= 1) return;

        int delta = (int) Math.max(1, Math.round(viewSize * ZOOM_FACTOR));
        int newSize = wheelRotation > 0
                ? Math.min(total, viewSize + delta)
                : Math.max(1, viewSize - delta);
        if (newSize == viewSize) return;

        Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
        double mouseRatio = dataArea.getWidth() > 0
                ? Math.clamp((mousePoint.getX() - dataArea.getMinX()) / dataArea.getWidth(), 0.0, 1.0)
                : 0.5;

        int anchorIndex = viewStart + (int) Math.round(mouseRatio * (viewSize - 1));
        viewStart = Math.clamp(anchorIndex - (int) Math.round(mouseRatio * (newSize - 1)), 0, total - newSize);
        viewSize = newSize;
        applyView();
    }

    private void panDomain(int wheelRotation) {
        int total = fullDataset.getColumnCount();
        int delta = (int) Math.max(1, Math.round(viewSize * PAN_FACTOR));
        int newStart = Math.clamp(viewStart + (wheelRotation > 0 ? delta : -delta), 0, total - viewSize);
        if (newStart == viewStart) return;
        viewStart = newStart;
        applyView();
    }

    @Override
    public void zoomToSelection(@NotNull Rectangle2D selectionRect, @NotNull ChartPanel panel) {
        Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
        if (dataArea.getWidth() <= 0 || viewSize <= 1) return;

        double relLeft = Math.clamp((selectionRect.getMinX() - dataArea.getMinX()) / dataArea.getWidth(), 0.0, 1.0);
        double relRight = Math.clamp((selectionRect.getMaxX() - dataArea.getMinX()) / dataArea.getWidth(), 0.0, 1.0);

        int newStart = viewStart + (int) Math.floor(relLeft * viewSize);
        int newEnd = viewStart + (int) Math.ceil(relRight * viewSize);
        newEnd = Math.min(newEnd, fullDataset.getColumnCount());
        int newSize = Math.max(1, newEnd - newStart);

        viewStart = newStart;
        viewSize = newSize;
        applyView();
    }

    private void applyView() {
        int end = Math.min(viewStart + viewSize, fullDataset.getColumnCount());
        DefaultCategoryDataset sliced = new DefaultCategoryDataset();
        for (int r = 0; r < fullDataset.getRowCount(); r++) {
            Comparable<?> rowKey = fullDataset.getRowKey(r);
            for (int c = viewStart; c < end; c++) {
                sliced.addValue(fullDataset.getValue(r, c), rowKey, fullDataset.getColumnKey(c));
            }
        }
        plot.setDataset(sliced);
    }
}

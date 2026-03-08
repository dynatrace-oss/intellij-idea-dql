package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import com.intellij.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
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
    private double dragAccumulator;

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
        double value = isHorizontal()
                ? plot.getRangeAxis().java2DToValue(p.getX(), dataArea, plot.getRangeAxisEdge())
                : plot.getRangeAxis().java2DToValue(p.getY(), dataArea, plot.getRangeAxisEdge());
        plot.setRangeCrosshairValue(value, true);
        return true;
    }

    @Override
    public boolean onMouseWheel(@NotNull MouseWheelEvent e, @NotNull ChartPanel panel) {
        if (e.isShiftDown()) {
            plot.panRangeAxes(e.getWheelRotation() * -0.1, panel.getChartRenderingInfo().getPlotInfo(), null);
        } else if (e.isControlDown()) {
            panDomain(e.getWheelRotation());
        } else {
            zoomDomain(e.getWheelRotation(), e.getPoint(), panel);
        }
        return false;
    }

    private void zoomDomain(int wheelRotation, @NotNull Point mousePoint, @NotNull ChartPanel panel) {
        int total = fullDataset.getColumnCount();
        if (total <= 1) {
            return;
        }

        int delta = (int) Math.max(1, Math.round(viewSize * ZOOM_FACTOR));
        int newSize = wheelRotation > 0
                ? Math.min(total, viewSize + delta)
                : Math.max(1, viewSize - delta);
        if (newSize == viewSize) {
            return;
        }

        Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
        double mouseRatio = domainRatio(mousePoint, dataArea);

        int anchorIndex = viewStart + (int) Math.round(mouseRatio * (viewSize - 1));
        int newStart = MathUtil.clamp(anchorIndex - (int) Math.round(mouseRatio * (newSize - 1)), 0, total - newSize);
        resizeView(newStart, newSize);
    }

    @Override
    public void zoomToSelection(@NotNull Rectangle2D selectionRect, @NotNull ChartPanel panel) {
        Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
        double axisLength = domainAxisLength(dataArea);
        if (axisLength <= 0 || viewSize <= 1) {
            return;
        }

        boolean horizontal = isHorizontal();
        double axisMin = horizontal ? dataArea.getMinY() : dataArea.getMinX();
        double relLeft, relRight;
        if (horizontal) {
            relLeft = 1.0 - MathUtil.clamp((selectionRect.getMaxY() - axisMin) / axisLength, 0.0, 1.0);
            relRight = 1.0 - MathUtil.clamp((selectionRect.getMinY() - axisMin) / axisLength, 0.0, 1.0);
        } else {
            relLeft = MathUtil.clamp((selectionRect.getMinX() - axisMin) / axisLength, 0.0, 1.0);
            relRight = MathUtil.clamp((selectionRect.getMaxX() - axisMin) / axisLength, 0.0, 1.0);
        }

        int newStart = viewStart + (int) Math.floor(relLeft * viewSize);
        int newEnd = Math.min(viewStart + (int) Math.ceil(relRight * viewSize), fullDataset.getColumnCount());
        resizeView(newStart, Math.max(1, newEnd - newStart));
    }

    private void resizeView(int newStart, int newSize) {
        viewStart = newStart;
        viewSize = newSize;
        applyView();
    }

    @Override
    public void onMiddleMouseReleased() {
        dragAccumulator = 0;
    }

    @Override
    public void onMiddleMouseDragged(@NotNull Point current, @NotNull Point last, @NotNull ChartPanel panel) {
        Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
        double axisLength = domainAxisLength(dataArea);
        if (axisLength <= 0 || viewSize <= 1) {
            return;
        }

        double dd = isHorizontal()
                ? -(current.getY() - last.getY())
                : (current.getX() - last.getX());
        dragAccumulator -= dd * viewSize / axisLength;

        int delta = (int) dragAccumulator;
        if (delta != 0) {
            dragAccumulator -= delta;
            shiftView(delta);
        }
    }

    private double domainAxisLength(@NotNull Rectangle2D dataArea) {
        return isHorizontal() ? dataArea.getHeight() : dataArea.getWidth();
    }

    private double domainRatio(@NotNull Point mousePoint, @NotNull Rectangle2D dataArea) {
        boolean horizontal = isHorizontal();
        double axisLength = horizontal ? dataArea.getHeight() : dataArea.getWidth();
        if (axisLength <= 0) {
            return 0.5;
        }
        double mousePos = horizontal ? mousePoint.getY() : mousePoint.getX();
        double axisMin = horizontal ? dataArea.getMinY() : dataArea.getMinX();
        double ratio = MathUtil.clamp((mousePos - axisMin) / axisLength, 0.0, 1.0);
        return horizontal ? 1.0 - ratio : ratio;
    }

    private void panDomain(int wheelRotation) {
        int delta = (int) Math.max(1, Math.round(viewSize * PAN_FACTOR));
        shiftView(wheelRotation > 0 ? delta : -delta);
    }

    private void shiftView(int delta) {
        if (delta == 0) {
            return;
        }
        int newStart = MathUtil.clamp(viewStart + delta, 0, fullDataset.getColumnCount() - viewSize);
        if (newStart == viewStart) {
            return;
        }
        viewStart = newStart;
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

    private boolean isHorizontal() {
        return plot.getOrientation() == PlotOrientation.HORIZONTAL;
    }
}

package pl.thedeem.intellij.common.components.charts.style;

import com.intellij.ui.JBColor;
import com.intellij.util.MathUtil;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.Range;
import org.jfree.data.category.SlidingCategoryDataset;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

class CategoryPlotInteractionHandler implements PlotInteractionHandler {
    private static final double ZOOM_FACTOR = 0.15;
    private static final double PAN_FACTOR = 0.1;

    private final CategoryPlot plot;
    private final SlidingCategoryDataset dataset;
    private double dragAccumulator;
    private Point selectionPressPoint;

    CategoryPlotInteractionHandler(@NotNull CategoryPlot plot) {
        this.plot = plot;
        this.dataset = (SlidingCategoryDataset) plot.getDataset();

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
        if (e.isControlDown()) {
            panDomain(e.getWheelRotation());
        } else {
            zoomDomain(e.getWheelRotation(), e.getPoint(), panel);
        }
        return false;
    }

    @Override
    public void onLeftMousePressed(@NotNull Point screenPoint, @NotNull ChartPanel panel) {
        selectionPressPoint = screenPoint;
    }

    void finishSelection(@NotNull Point releaseScreenPoint, @NotNull ChartPanel panel) {
        try {
            if (selectionPressPoint == null) {
                return;
            }
            Point2D pressJava2D = panel.translateScreenToJava2D(selectionPressPoint);
            Point2D releaseJava2D = panel.translateScreenToJava2D(releaseScreenPoint);
            double x = Math.min(pressJava2D.getX(), releaseJava2D.getX());
            double y = Math.min(pressJava2D.getY(), releaseJava2D.getY());
            double w = Math.abs(releaseJava2D.getX() - pressJava2D.getX());
            double h = Math.abs(releaseJava2D.getY() - pressJava2D.getY());
            if (w > 0 || h > 0) {
                zoomToSelection(new Rectangle2D.Double(x, y, w, h), panel);
            }
        } finally {
            selectionPressPoint = null;
        }
    }

    @Override
    public void paintOverlay(@NotNull Graphics2D g2, @NotNull ChartPanel panel) {
        if (selectionPressPoint == null) {
            return;
        }
        Point current = panel.getMousePosition();
        if (current == null) {
            return;
        }
        Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
        Point2D dataAreaMinScreen = panel.translateJava2DToScreen(new Point2D.Double(dataArea.getMinX(), dataArea.getMinY()));
        Point2D dataAreaMaxScreen = panel.translateJava2DToScreen(new Point2D.Double(dataArea.getMaxX(), dataArea.getMaxY()));

        Rectangle2D rect = createSelectionRectangle(current, dataAreaMinScreen, dataAreaMaxScreen);
        g2.setPaint(panel.getZoomFillPaint());
        g2.fill(rect);
        g2.setPaint(panel.getZoomOutlinePaint());
        g2.draw(rect);
    }

    private @NotNull Rectangle2D createSelectionRectangle(Point current, Point2D dataAreaMinScreen, Point2D dataAreaMaxScreen) {
        double x1, y1, width, height;
        if (isHorizontal()) {
            y1 = Math.min(selectionPressPoint.getY(), current.getY());
            height = Math.abs(current.getY() - selectionPressPoint.getY());
            x1 = dataAreaMinScreen.getX();
            width = dataAreaMaxScreen.getX() - dataAreaMinScreen.getX();
        } else {
            x1 = Math.min(selectionPressPoint.getX(), current.getX());
            width = Math.abs(current.getX() - selectionPressPoint.getX());
            y1 = dataAreaMinScreen.getY();
            height = dataAreaMaxScreen.getY() - dataAreaMinScreen.getY();
        }
        return new Rectangle2D.Double(x1, y1, width, height);
    }

    private void zoomToSelection(@NotNull Rectangle2D selectionRect, @NotNull ChartPanel panel) {
        Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
        int total = dataset.getUnderlyingDataset().getColumnCount();
        int viewSize = dataset.getMaximumCategoryCount();
        if (total <= 0 || viewSize <= 0) {
            return;
        }

        CategoryAxis axis = plot.getDomainAxis();
        int viewStart = dataset.getFirstCategoryIndex();
        int selectionStart = -1;
        int selectionEnd = viewStart + viewSize;

        for (int i = viewStart; i < viewStart + viewSize; i++) {
            double coord = axis.getCategoryMiddle(i - viewStart, viewSize, dataArea, plot.getDomainAxisEdge());
            boolean inside = isHorizontal()
                    ? coord >= selectionRect.getMinY() && coord <= selectionRect.getMaxY()
                    : coord >= selectionRect.getMinX() && coord <= selectionRect.getMaxX();
            if (inside) {
                if (selectionStart < 0) {
                    selectionStart = i;
                }
                selectionEnd = i + 1;
            }
        }

        if (selectionStart < 0) {
            return;
        }

        int clampedStart = MathUtil.clamp(selectionStart, 0, total - 1);
        int clampedEnd = MathUtil.clamp(selectionEnd, clampedStart + 1, total);
        resizeView(clampedStart, clampedEnd - clampedStart);
    }

    private void resizeView(int newStart, int newSize) {
        dataset.setFirstCategoryIndex(newStart);
        dataset.setMaximumCategoryCount(newSize);
    }

    @Override
    public void onMiddleMouseReleased() {
        dragAccumulator = 0;
    }

    @Override
    public void onMiddleMouseDragged(@NotNull Point current, @NotNull Point last, @NotNull ChartPanel panel) {
        Rectangle2D dataArea = panel.getChartRenderingInfo().getPlotInfo().getDataArea();
        double axisLength = domainAxisLength(dataArea);
        int viewSize = dataset.getMaximumCategoryCount();
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

    private void zoomDomain(int wheelRotation, @NotNull Point mousePoint, @NotNull ChartPanel panel) {
        int total = dataset.getUnderlyingDataset().getColumnCount();
        int viewSize = dataset.getMaximumCategoryCount();
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
        Point2D java2DPoint = panel.translateScreenToJava2D(mousePoint);
        double mouseRatio = domainRatio(java2DPoint, dataArea);

        int viewStart = dataset.getFirstCategoryIndex();
        double anchorPos = viewStart + mouseRatio * (viewSize - 1);
        int newStart = MathUtil.clamp((int) Math.round(anchorPos - mouseRatio * (newSize - 1)), 0, total - newSize);
        resizeView(newStart, newSize);
    }

    private double domainAxisLength(@NotNull Rectangle2D dataArea) {
        return isHorizontal() ? dataArea.getHeight() : dataArea.getWidth();
    }

    private double domainRatio(@NotNull Point2D mousePoint, @NotNull Rectangle2D dataArea) {
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
        int viewSize = dataset.getMaximumCategoryCount();
        int delta = (int) Math.max(1, Math.round(viewSize * PAN_FACTOR));
        shiftView(wheelRotation > 0 ? delta : -delta);
    }

    private void shiftView(int delta) {
        if (delta == 0) {
            return;
        }
        int total = dataset.getUnderlyingDataset().getColumnCount();
        int viewSize = dataset.getMaximumCategoryCount();
        int viewStart = dataset.getFirstCategoryIndex();
        int newStart = MathUtil.clamp(viewStart + delta, 0, total - viewSize);
        if (newStart != viewStart) {
            dataset.setFirstCategoryIndex(newStart);
        }
    }

    private boolean isHorizontal() {
        return plot.getOrientation() == PlotOrientation.HORIZONTAL;
    }
}

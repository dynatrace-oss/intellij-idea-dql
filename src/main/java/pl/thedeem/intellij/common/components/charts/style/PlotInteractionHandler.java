package pl.thedeem.intellij.common.components.charts.style;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartPanel;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

interface PlotInteractionHandler {
    Stroke CROSSHAIR_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3.0f}, 0);

    boolean onMouseMoved(@NotNull Point2D p, @NotNull Rectangle2D dataArea);

    boolean onMouseWheel(@NotNull MouseWheelEvent e, @NotNull ChartPanel panel);

    default void onMiddleMouseDragged(@NotNull Point current, @NotNull Point last, @NotNull ChartPanel panel) {
    }

    default void onMiddleMouseReleased() {
    }

    default void zoomToSelection(@NotNull Rectangle2D selectionRect, @NotNull ChartPanel panel) {
    }
}


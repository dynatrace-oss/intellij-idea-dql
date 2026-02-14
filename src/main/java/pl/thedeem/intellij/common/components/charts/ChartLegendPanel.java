package pl.thedeem.intellij.common.components.charts;

import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class ChartLegendPanel extends JBScrollPane {
    private final JFreeChart chart;
    private final DefaultListModel<LegendItemData> listModel = new DefaultListModel<>();
    private final JBList<LegendItemData> legendList = new JBList<>(listModel);
    private String hoveredSeries = null;

    public ChartLegendPanel(@NotNull JFreeChart chart, boolean horizontal) {
        this.chart = chart;

        legendList.setCellRenderer(new LegendItemRenderer());
        legendList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        legendList.setLayoutOrientation(horizontal ? JList.HORIZONTAL_WRAP : JList.VERTICAL);
        legendList.setVisibleRowCount(horizontal ? 0 : -1);

        buildLegendItems();
        setupListeners();

        ListSpeedSearch.installOn(legendList, LegendItemData::name);

        setViewportView(legendList);
        setBorder(JBUI.Borders.empty());
        setOpaque(false);
        getViewport().setOpaque(false);
    }

    private void buildLegendItems() {
        LegendItemCollection items = chart.getPlot().getLegendItems();
        if (items == null) return;

        for (int i = 0; i < items.getItemCount(); i++) {
            LegendItem item = items.get(i);
            if (item != null) {
                Color color = item.getFillPaint() instanceof Color c ? c : JBColor.BLUE;
                listModel.addElement(new LegendItemData(item.getLabel(), color, true));
            }
        }
    }

    private void setupListeners() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handlePoint(e.getPoint());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handlePoint(e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                updateHoverState(-1);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int index = getIndexAtPoint(e.getPoint());
                    if (index >= 0) toggleSeriesVisibility(index);
                }
            }

            private void handlePoint(Point p) {
                updateHoverState(getIndexAtPoint(p));
            }
        };
        legendList.addMouseListener(adapter);
        legendList.addMouseMotionListener(adapter);
    }

    private int getIndexAtPoint(Point point) {
        int index = legendList.locationToIndex(point);
        if (index >= 0) {
            Rectangle bounds = legendList.getCellBounds(index, index);
            if (bounds != null && bounds.contains(point)) return index;
        }
        return -1;
    }

    private void updateHoverState(int index) {
        String name = (index >= 0) ? listModel.get(index).name() : null;
        if (Objects.equals(hoveredSeries, name)) return;

        hoveredSeries = name;
        syncChartWithModel();
        legendList.repaint();
    }

    private void toggleSeriesVisibility(int index) {
        LegendItemData old = listModel.get(index);
        listModel.set(index, new LegendItemData(old.name(), old.color(), !old.visible()));
        SwingUtilities.invokeLater(this::syncChartWithModel);
    }

    private void syncChartWithModel() {
        chart.setNotify(false);
        try {
            Plot plot = chart.getPlot();
            for (int i = 0; i < listModel.size(); i++) {
                LegendItemData data = listModel.get(i);
                float alpha = !data.visible() ? 0.12f : (hoveredSeries == null || data.name().equals(hoveredSeries) ? 1.0f : 0.25f);
                Color paint = ColorUtil.toAlpha(data.color(), (int) (255 * alpha));

                if (plot instanceof PiePlot<?> pie) {
                    pie.setSectionPaint(data.name(), paint);
                } else {
                    applyToRenderer(plot, i, (renderer, idx) -> {
                        renderer.setSeriesPaint(idx, paint);
                        renderer.setSeriesVisible(idx, data.visible());
                    });
                }
            }
        } finally {
            chart.setNotify(true);
        }
    }

    private void applyToRenderer(Plot plot, int index, java.util.function.BiConsumer<AbstractRenderer, Integer> action) {
        if (plot instanceof XYPlot xy && xy.getRenderer() instanceof AbstractRenderer r) action.accept(r, index);
        else if (plot instanceof CategoryPlot cp && cp.getRenderer() instanceof AbstractRenderer r)
            action.accept(r, index);
    }

    private record LegendItemData(@NotNull String name, @NotNull Color color, boolean visible) {
    }

    private class LegendItemRenderer extends JPanel implements ListCellRenderer<LegendItemData> {
        private final JLabel label = new JLabel();
        private final JPanel colorBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(JBColor.border());
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
            }
        };

        LegendItemRenderer() {
            setLayout(new BorderLayout(JBUI.scale(8), 0));
            setBorder(JBUI.Borders.empty(4, 8));
            setOpaque(true);
            colorBox.setPreferredSize(new Dimension(JBUI.scale(10), JBUI.scale(10)));
            add(colorBox, BorderLayout.WEST);
            add(label, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends LegendItemData> list, LegendItemData value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            boolean isHovered = Objects.equals(value.name(), hoveredSeries);

            setBackground(isHovered || isSelected ? JBUI.CurrentTheme.List.Selection.background(true) : list.getBackground());
            colorBox.setBackground(value.visible() ? value.color() : ColorUtil.toAlpha(value.color(), 60));

            label.setText(DQLBundle.shorten(value.name(), 45));
            label.setFont(JBUI.Fonts.label().deriveFont(isHovered ? Font.BOLD : Font.PLAIN));
            label.setForeground(value.visible() ? JBUI.CurrentTheme.Label.foreground() : JBColor.GRAY);

            return this;
        }
    }
}
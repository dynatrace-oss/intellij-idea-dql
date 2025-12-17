package pl.thedeem.intellij.dql.components.common;

import pl.thedeem.intellij.dql.components.DQLComponentUtils;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
    public MultiLineCellRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
        setOpaque(true);
        setBorder(BorderFactory.createCompoundBorder(getBorder(), DQLComponentUtils.DEFAULT_BORDER));
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText(value == null ? "" : value.toString());
        setFont(table.getFont());
        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
        return this;
    }

    public static void installOn(JTable table) {
        MultiLineCellRenderer renderer = new MultiLineCellRenderer();
        table.setDefaultRenderer(Object.class, renderer);
        table.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                adjustRowHeights(table, renderer);
            }
        });
        SwingUtilities.invokeLater(() -> adjustRowHeights(table, renderer));
    }

    private static void adjustRowHeights(JTable table, MultiLineCellRenderer renderer) {
        table.setRowHeight(table.getRowHeight()); // Reset to default if needed, or skip

        for (int row = 0; row < table.getRowCount(); row++) {
            int rowHeight = table.getRowHeight(row);
            int preferredRowHeight = 0;
            for (int column = 0; column < table.getColumnCount(); column++) {
                Object value = table.getValueAt(row, column);
                Component rendererComponent = renderer.getTableCellRendererComponent(
                        table, value, false, false, row, column
                );

                TableColumnModel columnModel = table.getColumnModel();
                rendererComponent.setSize(columnModel.getColumn(column).getWidth(), Short.MAX_VALUE);

                preferredRowHeight = Math.max(preferredRowHeight, rendererComponent.getPreferredSize().height);
            }
            preferredRowHeight += table.getRowMargin();
            if (rowHeight != preferredRowHeight) {
                table.setRowHeight(row, preferredRowHeight);
            }
        }
    }
}

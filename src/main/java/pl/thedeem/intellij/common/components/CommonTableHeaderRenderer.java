package pl.thedeem.intellij.common.components;

import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class CommonTableHeaderRenderer extends DefaultTableCellRenderer {
    private final TableCellRenderer defaultRenderer;

    public CommonTableHeaderRenderer(TableCellRenderer defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = defaultRenderer != null ?
                defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                : super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel label) {
            label.setBorder(JBUI.Borders.compound(label.getBorder(), ComponentsUtils.DEFAULT_BORDER));
            label.setFont(label.getFont().deriveFont(Font.BOLD));
            label.setBorder(ComponentsUtils.DEFAULT_BORDER);
            return c;
        }
        return c;
    }
}

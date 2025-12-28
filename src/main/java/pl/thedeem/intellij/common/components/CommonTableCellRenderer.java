package pl.thedeem.intellij.common.components;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class CommonTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JComponent jc) {
            jc.setBorder(BorderFactory.createCompoundBorder(jc.getBorder(), ComponentsUtils.DEFAULT_BORDER));
        }
        return c;
    }
}

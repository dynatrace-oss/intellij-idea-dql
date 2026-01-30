package pl.thedeem.intellij.common.components.table;

import com.intellij.ui.table.JBTable;
import pl.thedeem.intellij.common.components.ComponentsUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RowCountTable extends JBTable {
    public RowCountTable(JTable mainTable) {
        super(new AbstractTableModel() {
            @Override
            public int getRowCount() {
                return mainTable.getRowCount();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public Object getValueAt(int row, int column) {
                return row + 1;
            }
        });

        setRowHeight(mainTable.getRowHeight());
        setSelectionModel(mainTable.getSelectionModel());
        setTableHeader(null);
        setFocusable(false);
        setCellSelectionEnabled(false);
        setColumnSelectionAllowed(false);
        ComponentsUtils.addCopyingCellValuesSupport(this, mainTable);
        setPreferredScrollableViewportSize(new Dimension(
                (getFontMetrics(getFont()).charWidth('0') * String.valueOf(mainTable.getRowCount()).length()) + 10,
                0
        ));
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}

package pl.thedeem.intellij.common.components.table;

import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.table.JBTable;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.components.ComponentsUtils;
import pl.thedeem.intellij.common.components.table.rendering.CommonTableCellRenderer;
import pl.thedeem.intellij.common.components.table.rendering.CommonTableHeaderRenderer;
import pl.thedeem.intellij.common.components.table.reordering.TableColumnReorderUtil;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;

public class CommonTable extends JBTable {
    public CommonTable(@NotNull TableModel model) {
        super(model);
        setLayout(new BorderLayout());
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        ComponentsUtils.addCopyingCellValuesSupport(this);
        TableSpeedSearch.installOn(this);
        setDefaultRenderer(Object.class, new CommonTableCellRenderer());
        getTableHeader().setDefaultRenderer(new CommonTableHeaderRenderer(getTableHeader().getDefaultRenderer()));
    }

    public void addRightClickSelection() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && getSelectedRowCount() <= 1) {
                    int row = rowAtPoint(e.getPoint());
                    int column = columnAtPoint(e.getPoint());

                    if (row != -1 && column != -1) {
                        setRowSelectionInterval(row, row);
                        setColumnSelectionInterval(column, column);
                    }
                }
            }
        });
    }

    public void setColumnPreferredWidthInCharacters(int column, int characters) {
        if (column < 0 || column > getColumnCount()) {
            return;
        }
        getColumnModel().getColumn(column).setPreferredWidth(getFontMetrics(getFont()).charWidth('0') * characters + 10);
    }

    public @NotNull JBPopup createColumnsReorderPopup(@NotNull Set<String> allColumns) {
        JComponent content = new TableColumnReorderUtil().createColumnReorderingComponent(this, allColumns);
        return JBPopupFactory.getInstance()
                .createComponentPopupBuilder(content, this)
                .setRequestFocus(true)
                .setResizable(true)
                .setMovable(true)
                .setCancelOnClickOutside(true)
                .setCancelOnOtherWindowOpen(true)
                .createPopup();
    }
}

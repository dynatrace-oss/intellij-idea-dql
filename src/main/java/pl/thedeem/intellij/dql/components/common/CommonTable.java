package pl.thedeem.intellij.dql.components.common;

import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.table.JBTable;
import pl.thedeem.intellij.dql.components.DQLComponentUtils;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CommonTable extends JBTable {
   public CommonTable(TableModel model) {
      super(model);
      setLayout(new BorderLayout());
      setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      DQLComponentUtils.addCopyingCellValuesSupport(this);
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
}

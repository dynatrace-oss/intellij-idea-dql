package pl.thedeem.intellij.dql.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.TableSpeedSearch;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.sdk.model.DQLRecord;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;
import java.util.function.IntFunction;

public class DQLTableResultPanel extends JPanel {
   protected final static ObjectMapper mapper = JsonMapper.builder().build();
   private final static int DEFAULT_STRING_COLUMN_SIZE = 250;

   private final Map<String, String> columnTypes;
   private final Set<String> columns;

   public DQLTableResultPanel(DQLResult result) {
      super();
      columns = result.getColumns();
      columnTypes = result.getColumnTypes();
      setLayout(new BorderLayout());
      JBTable table = createTableComponent(result);
      add(createTableView(table), BorderLayout.CENTER);
      add(createTableSummary(result, table), BorderLayout.SOUTH);
   }

   @SuppressWarnings("unchecked") // due to new ColumnInfo[columns.size()]
   private ColumnInfo<DQLRecord, Object>[] getColumns() {
      return columns.stream().map(s -> new ColumnInfo<DQLRecord, Object>(s) {
         @Override
         public @Nullable Object valueOf(DQLRecord record) {
            Object value = record.get(s);
            if (value == null) return null;
            String stringValue = value.toString();
            String columnValue = columnTypes.get(s);
            try {
               return switch (columnValue) {
                  case "double", "number" -> Double.valueOf(stringValue);
                  case "boolean" -> Boolean.valueOf(stringValue);
                  case "long" -> Long.valueOf(stringValue);
                  case "array", "record", "timeframe" -> {
                     try {
                        yield mapper.writeValueAsString(value);
                     } catch (JsonProcessingException e) {
                        yield stringValue;
                     }
                  }
                  case "timestamp" -> DQLUtil.getDateFromTimestamp(stringValue);
                  default -> stringValue;
               };
            } catch (NumberFormatException e) {
               return null;
            }
         }

         @Override
         public Class<?> getColumnClass() {
            String columnValue = columnTypes.get(s);
            return switch (columnValue) {
               case "double", "number", "long" -> Double.class;
               case "boolean" -> Boolean.class;
               case "timestamp" -> ZonedDateTime.class;
               default -> String.class;
            };
         }
      }).toArray((IntFunction<ColumnInfo<DQLRecord, Object>[]>) value -> new ColumnInfo[columns.size()]);
   }

   private JTable createRecordNumberTable(JBTable mainTable) {
      JTable rowTable = createRowCountTable(mainTable);
      rowTable.setRowHeight(mainTable.getRowHeight());
      rowTable.setSelectionModel(mainTable.getSelectionModel());
      rowTable.setTableHeader(null);
      rowTable.setFocusable(false);
      rowTable.setCellSelectionEnabled(false);
      rowTable.setColumnSelectionAllowed(false);
      addContextMenu(rowTable);
      addCopyingCellValues(rowTable, mainTable);
      int rowCount = mainTable.getRowCount();
      FontMetrics metrics = rowTable.getFontMetrics(rowTable.getFont());
      int maxDigits = String.valueOf(rowCount).length();
      rowTable.setPreferredScrollableViewportSize(new Dimension(metrics.charWidth('0') * maxDigits + 10, 0));
      rowTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            return c;
         }
      });
      return rowTable;
   }

   private JBScrollPane createTableView(JBTable table) {
      JTable recordNumberTable = createRecordNumberTable(table);
      JBScrollPane res = new JBScrollPane(table);
      res.setRowHeaderView(recordNumberTable);
      res.getRowHeader().setPreferredSize(recordNumberTable.getPreferredScrollableViewportSize());
      res.setBorder(BorderFactory.createEmptyBorder());
      return res;
   }

   private @NotNull JBTable createTableComponent(DQLResult result) {
      JBTable table = new JBTable(new ListTableModel<>(getColumns(), new ArrayList<>(result.getRecords()), 0));
      table.setLayout(new BorderLayout());
      table.setAutoCreateRowSorter(true);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      addContextMenu(table);
      addCopyingCellValues(table, table);
      addCellRenderer(table);
      addHeaderRenderer(table);
      TableSpeedSearch.installOn(table);

      TableColumnModel columnModel = table.getColumnModel();
      for (int i = 0; i < columnModel.getColumnCount(); i++) {
         TableColumn column = columnModel.getColumn(i);
         Class<?> columnClass = table.getColumnClass(i);
         if (columnClass == String.class) {
            column.setPreferredWidth(DEFAULT_STRING_COLUMN_SIZE);
         }
      }
      return table;
   }

   private static @NotNull JTable createRowCountTable(JBTable mainTable) {
      return new JTable(new AbstractTableModel() {
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
      }) {
         @Override
         public boolean isCellEditable(int row, int column) {
            return false;
         }
      };
   }

   private void addContextMenu(JTable table) {
      // to automatically select the cell where user right-clicks
      table.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            if (SwingUtilities.isRightMouseButton(e) && table.getSelectedRowCount() <= 1) {
               int row = table.rowAtPoint(e.getPoint());
               int column = table.columnAtPoint(e.getPoint());

               if (row != -1 && column != -1) {
                  table.setRowSelectionInterval(row, row);
                  table.setColumnSelectionInterval(column, column);
               }
            }
         }
      });

      JPopupMenu popupMenu = new JPopupMenu();
      popupMenu.add(createOpenValueAction(table));
      popupMenu.add(createOpenDqlRecordDetailsAction(table));
      popupMenu.addSeparator();
      popupMenu.add(createCopyRowAction(table));
      table.setComponentPopupMenu(popupMenu);
   }

   @SuppressWarnings("unchecked")
   protected JMenuItem createOpenDqlRecordDetailsAction(JTable table) {
      JMenuItem action = new JMenuItem(DQLBundle.message("components.dqlRecordDetails.actionName"));
      action.setIcon(AllIcons.Actions.Properties);
      action.setBorder(DQLComponentUtils.DEFAULT_BORDER);
      action.addActionListener(e -> {
         int row = table.getSelectedRow();

         Map<String, Object> rowValues = new HashMap<>();
         for (int col = 0; col < table.getColumnCount(); col++) {
            rowValues.put(table.getColumnName(col), prepareColumnValue(table.getValueAt(row, col)));
         }

         ColumnInfo<Map.Entry<String, Object>, Object>[] columnInfos = new ColumnInfo[2];
         columnInfos[0] = new ColumnInfo<>(DQLBundle.message("components.dqlRecordDetails.columns.field")) {
            @Override
            public @Nullable Object valueOf(Map.Entry<String, Object> s) {
               return s.getKey();
            }
         };
         columnInfos[1] = new ColumnInfo<>(DQLBundle.message("components.dqlRecordDetails.columns.fieldValue")) {
            @Override
            public @Nullable Object valueOf(Map.Entry<String, Object> s) {
               return s.getValue();
            }
         };

         JBTable tableResults = new JBTable(new ListTableModel<>(columnInfos, rowValues.entrySet().stream().toList(), 0));
         tableResults.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
         createOpenValueAction(tableResults);
         TableSpeedSearch.installOn(tableResults);
         addCellRenderer(tableResults);
         JBScrollPane scroll = new JBScrollPane(tableResults);
         scroll.setPreferredSize(new Dimension(600, 400));
         JOptionPane.showMessageDialog(
             this,
             scroll,
             DQLBundle.message("components.dqlRecordDetails.title"),
             JOptionPane.INFORMATION_MESSAGE,
             AllIcons.Actions.Properties
         );
      });
      return action;
   }

   protected JMenuItem createCopyRowAction(JTable table) {
      JMenuItem action = new JMenuItem(DQLBundle.message("components.tableResults.actions.copySelectedRows"));
      action.setIcon(AllIcons.General.Copy);
      action.setBorder(DQLComponentUtils.DEFAULT_BORDER);
      action.addActionListener(e -> {
         int row = table.getSelectedRow();
         int count = table.getSelectedRowCount();
         String content = getJsonContentForRows(table, row, count);
         if (StringUtil.isNotEmpty(content)) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content), null);
         }
      });
      return action;
   }

   protected JMenuItem createOpenValueAction(JTable table) {
      JMenuItem action = new JMenuItem(DQLBundle.message("components.tableResults.actions.openCellDetails"));
      action.setIcon(AllIcons.General.OpenInToolWindow);
      action.setBorder(DQLComponentUtils.DEFAULT_BORDER);
      action.addActionListener(e -> {
         int row = table.getSelectedRow();
         int col = table.getSelectedColumn();
         if (row != -1 && col != -1) {
            Object value = table.getValueAt(row, col);

            JTextArea textArea = new JTextArea(value != null ? value.toString() : "");
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));

            JOptionPane.showMessageDialog(
                null,
                scrollPane,
                DQLBundle.message("components.tableResults.cellDetails.title", table.getColumnName(col)),
                JOptionPane.INFORMATION_MESSAGE
            );
         }
      });
      return action;
   }

   private Object prepareColumnValue(Object value) {
      if (value instanceof ZonedDateTime zonedDateTime) {
         return zonedDateTime.format(DQLUtil.DQL_DATE_FORMATTER);
      }
      return value;
   }

   @Nullable
   private String getJsonContentForRows(JTable table, int startRow, int rowCount) {
      if (startRow != -1) {
         List<Map<String, Object>> results = new ArrayList<>();
         for (int i = startRow; i < startRow + rowCount; i++) {
            Map<String, Object> rowValues = new HashMap<>();
            for (int col = 0; col < table.getColumnCount(); col++) {
               rowValues.put(table.getColumnName(col), prepareColumnValue(table.getValueAt(i, col)));
            }
            results.add(rowValues);
         }
         try {
            return mapper.writeValueAsString(results);
         } catch (JsonProcessingException ex) {
            return null;
         }
      }
      return null;
   }


   private void addCopyingCellValues(JTable table, JTable copyFromTable) {
      KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
      table.getInputMap().put(copy, "copy");
      table.getActionMap().put("copy", new AbstractAction() {
         @Override
         public void actionPerformed(ActionEvent e) {
            int row = copyFromTable.getSelectedRow();
            int col = copyFromTable.getSelectedColumn();
            if (row != -1 && col != -1) {
               Object value = prepareColumnValue(copyFromTable.getValueAt(row, col));
               StringSelection selection = new StringSelection(value != null ? value.toString() : "");
               Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            }
         }
      });
   }

   private void addCellRenderer(JBTable table) {
      table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JComponent jc && value != null) {
               jc.setBorder(BorderFactory.createCompoundBorder(jc.getBorder(), DQLComponentUtils.DEFAULT_BORDER));
               if (value instanceof ZonedDateTime zonedDateTime) {
                  setText(zonedDateTime.format(DQLUtil.USER_FRIENDLY_DATE_FORMATTER));
               }
            }
            return c;
         }
      });
   }

   private void addHeaderRenderer(JBTable table) {
      JTableHeader header = table.getTableHeader();

      TableCellRenderer defaultRenderer = header.getDefaultRenderer();
      header.setDefaultRenderer(new DefaultTableCellRenderer() {
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel label) {
               String columnName = table.getColumnName(column);
               String columnType = columnTypes.get(columnName);
               JLabel result = label;
               if (label.getIcon() == null && StringUtil.isNotEmpty(columnType)) {
                  Icon icon = switch (columnType) {
                     case "long", "double", "number" -> DQLIcon.DQL_NUMBER;
                     case "array" -> DQLIcon.DQL_ARRAY;
                     case "record" -> DQLIcon.DQL_RECORD;
                     case "boolean" -> DQLIcon.DQL_BOOLEAN;
                     case "timestamp", "timeframe" -> DQLIcon.DQL_TIME_FIELD;
                     default -> DQLIcon.DQL_FIELD;
                  };
                  result = new JLabel(label.getText(), icon, JLabel.LEFT);
                  result.setToolTipText(columnType);
               }

               result.setFont(label.getFont().deriveFont(Font.BOLD));
               result.setHorizontalAlignment(SwingConstants.CENTER);
               result.setBorder(DQLComponentUtils.DEFAULT_BORDER);
               return result;
            }
            return c;
         }
      });
   }

   private JBScrollPane createTableSummary(DQLResult result, JBTable table) {
      JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      summaryPanel.setBorder(BorderFactory.createEmptyBorder());
      JLabel tableSummary = new JLabel(DQLBundle.message(
          "components.dqlResults.summary.description",
          result.getRecords().size(),
          result.getGrailMetadata().executionTimeMilliseconds
      ), AllIcons.General.Information, JLabel.LEFT);
      tableSummary.setBorder(DQLComponentUtils.DEFAULT_BORDER);
      table.getModel().addTableModelListener(e -> tableSummary.setText(DQLBundle.message(
          "components.dqlResults.summary.description",
          result.getRecords().size(),
          result.getGrailMetadata().executionTimeMilliseconds
      )));
      summaryPanel.add(tableSummary, BorderLayout.WEST);

      for (DQLResult.DQLNotification notification : result.getGrailMetadata().getNotifications()) {
         JPanel notificationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         notificationPanel.setBorder(BorderFactory.createEmptyBorder());
         notificationPanel.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.WEST);
         notificationPanel.add(new JLabel(notification.getMessage(), AllIcons.General.Warning, JLabel.LEFT), BorderLayout.EAST);
         summaryPanel.add(notificationPanel, BorderLayout.EAST);
      }

      JBScrollPane res = new JBScrollPane(summaryPanel);
      res.setBorder(BorderFactory.createEmptyBorder());
      return res;
   }
}

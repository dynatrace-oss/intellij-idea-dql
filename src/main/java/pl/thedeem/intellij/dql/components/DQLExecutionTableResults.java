package pl.thedeem.intellij.dql.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLRecord;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.IntFunction;

public class DQLExecutionTableResults extends JPanel {
    private final static int DEFAULT_STRING_COLUMN_SIZE = 250;

    private final Map<String, String> columns;
    private final TableResults<DQLRecord> tableResults;

    public DQLExecutionTableResults(DQLResult result) {
        super();
        columns = result.getColumns();
        tableResults = new TableResults<>(new ListTableModel<>(getColumns(), new ArrayList<>(result.getRecords()), 0));
        tableResults.setLayout(new BorderLayout());
        TableColumnModel columnModel = tableResults.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            Class<?> columnClass = tableResults.getColumnClass(i);
            if (columnClass == String.class) {
                column.setPreferredWidth(DEFAULT_STRING_COLUMN_SIZE);
            }
        }
        addHeaderRenderer();
        setLayout(new BorderLayout());
        add(createTableView(), BorderLayout.CENTER);
        add(createTableSummary(result), BorderLayout.SOUTH);
    }

    private void addHeaderRenderer() {
        JTableHeader header = tableResults.getTableHeader();

        TableCellRenderer defaultRenderer = header.getDefaultRenderer();
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JLabel label) {
                    String columnName = tableResults.getColumnName(column);
                    String columnType = columns.get(columnName);
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

    @SuppressWarnings("unchecked") // due to new ColumnInfo[columns.size()]
    private ColumnInfo<DQLRecord, Object>[] getColumns() {
        return columns.keySet().stream().map(s -> new ColumnInfo<DQLRecord, Object>(s) {
            @Override
            public @Nullable Object valueOf(DQLRecord record) {
                Object value = record.get(s);
                if (value == null) return null;
                String stringValue = value.toString();
                String columnValue = columns.get(s);
                try {
                    return switch (columnValue) {
                        case "double", "number" -> Double.valueOf(stringValue);
                        case "boolean" -> Boolean.valueOf(stringValue);
                        case "long" -> Long.valueOf(stringValue);
                        case "array", "record", "timeframe" -> {
                            try {
                                yield TableResults.mapper.writeValueAsString(value);
                            } catch (JsonProcessingException e) {
                                yield stringValue;
                            }
                        }
                        default -> stringValue;
                    };
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            @Override
            public Class<?> getColumnClass() {
                String columnValue = columns.get(s);
                return switch (columnValue) {
                    case "double", "number" -> Double.class;
                    case "boolean" -> Boolean.class;
                    case "long" -> Long.class;
                    default -> String.class;
                };
            }
        }).toArray((IntFunction<ColumnInfo<DQLRecord, Object>[]>) value -> new ColumnInfo[columns.size()]);
    }

    private JBScrollPane createTableView() {
        tableResults.configureCellsRendering();
        tableResults.setCopyCellValues();
        tableResults.addContextMenu();
        tableResults.addSorting();

        JTable recordNumberTable = getRecordNumberTable(tableResults);
        JBScrollPane res = new JBScrollPane(tableResults);
        res.setRowHeaderView(recordNumberTable);
        res.getRowHeader().setPreferredSize(recordNumberTable.getPreferredScrollableViewportSize());
        res.setBorder(BorderFactory.createEmptyBorder());
        return res;
    }

    private JTable getRecordNumberTable(TableResults<DQLRecord> mainTable) {
        JTable rowTable = createRowCountTable(mainTable);
        rowTable.setRowHeight(mainTable.getRowHeight());
        rowTable.setSelectionModel(mainTable.getSelectionModel());
        rowTable.setTableHeader(null);
        rowTable.setFocusable(false);
        rowTable.setCellSelectionEnabled(false);
        rowTable.setColumnSelectionAllowed(false);
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

    private static @NotNull JTable createRowCountTable(TableResults<DQLRecord> mainTable) {
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

    private JBScrollPane createTableSummary(DQLResult result) {
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder());
        JLabel tableSummary = new JLabel(DQLBundle.message(
                "components.dqlResults.summary.description",
                tableResults.getModel().getRowCount(),
                result.getGrailMetadata().executionTimeMilliseconds
        ), AllIcons.General.Information, JLabel.LEFT);
        tableSummary.setBorder(DQLComponentUtils.DEFAULT_BORDER);
        tableResults.getModel().addTableModelListener(e -> tableSummary.setText(DQLBundle.message(
                "components.dqlResults.summary.description",
                tableResults.getModel().getRowCount(),
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

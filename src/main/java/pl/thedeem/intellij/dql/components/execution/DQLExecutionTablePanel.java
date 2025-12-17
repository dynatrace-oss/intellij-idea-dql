package pl.thedeem.intellij.dql.components.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.components.DQLComponentUtils;
import pl.thedeem.intellij.dql.components.common.CommonTable;
import pl.thedeem.intellij.dql.components.common.CommonTableCellRenderer;
import pl.thedeem.intellij.dql.components.common.CommonTableHeaderRenderer;
import pl.thedeem.intellij.dql.components.common.RowCountTable;
import pl.thedeem.intellij.dql.fileProviders.DQLRecordFieldVirtualFile;
import pl.thedeem.intellij.dql.fileProviders.DQLRecordVirtualFile;
import pl.thedeem.intellij.dql.sdk.model.DQLRecord;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class DQLExecutionTablePanel extends JPanel {
    protected final static ObjectMapper mapper = JsonMapper.builder().build();

    private final Map<String, String> columnTypes;
    private final Project project;
    private final Set<String> columns;

    public DQLExecutionTablePanel(@NotNull Project project, @NotNull DQLResult result) {
        super();
        this.project = project;
        columns = result.getColumns();
        columnTypes = result.getColumnTypes();
        setLayout(new BorderLayout());
        CommonTable table = createTableComponent(result);
        add(createTableView(table), BorderLayout.CENTER);
        add(createTableSummary(result, table), BorderLayout.SOUTH);
    }

    private JBScrollPane createTableView(CommonTable table) {
        JTable recordNumberTable = new RowCountTable(table);
        JBScrollPane result = new JBScrollPane(table);
        result.setRowHeaderView(recordNumberTable);
        result.getRowHeader().setPreferredSize(recordNumberTable.getPreferredScrollableViewportSize());
        result.setBorder(BorderFactory.createEmptyBorder());

        recordNumberTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openSelectedRow(table);
                }
            }
        });
        return result;
    }

    private @NotNull CommonTable createTableComponent(DQLResult result) {
        List<ColumnInfo<DQLRecord, Object>> columnInfos = calculateColumns();
        CommonTable table = new CommonTable(new ListTableModel<>(columnInfos.toArray(new ColumnInfo[]{}), new ArrayList<>(result.getRecords()), 0));
        table.setAutoCreateRowSorter(true);
        table.addRightClickSelection();
        table.setDefaultRenderer(Object.class, new DQLCellRenderer());
        table.getTableHeader().setDefaultRenderer(new DQLHeaderRenderer(columnTypes, table.getTableHeader()));
        updateColumnSizes(table);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    JTable target = (JTable) e.getSource();
                    openSelectedCell(target);
                }
            }
        });

        return table;
    }

    private void updateColumnSizes(CommonTable table) {
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            String type = columnTypes.get(column.getHeaderValue().toString());
            switch (type) {
                case "double", "number", "long", "duration" -> table.setColumnPreferredWidthInCharacters(i, 10);
                case "array", "record", "timeframe" -> table.setColumnPreferredWidthInCharacters(i, 50);
                case "timestamp" ->
                        table.setColumnPreferredWidthInCharacters(i, ZonedDateTime.now().format(DQLUtil.USER_FRIENDLY_DATE_FORMATTER).length());
                case "string" -> table.setColumnPreferredWidthInCharacters(i, 30);
                case "ip_address" -> table.setColumnPreferredWidthInCharacters(i, 15);
                case "uid" -> table.setColumnPreferredWidthInCharacters(i, 32);
            }
        }
    }

    private List<ColumnInfo<DQLRecord, Object>> calculateColumns() {
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
                        case "timestamp" -> {
                            try {
                                yield DQLUtil.getDateFromTimestamp(stringValue);
                            } catch (DateTimeParseException e) {
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
                String columnValue = columnTypes.get(s);
                return switch (columnValue) {
                    case "double", "number", "long" -> Double.class;
                    case "boolean" -> Boolean.class;
                    case "timestamp" -> ZonedDateTime.class;
                    default -> String.class;
                };
            }
        }).collect(Collectors.toList());
    }

    private JBScrollPane createTableSummary(DQLResult result, JBTable table) {
        DQLExecutionSummary summaryPanel = new DQLExecutionSummary(result);
        table.getModel().addTableModelListener(e -> summaryPanel.refreshDescription());
        JBScrollPane res = new JBScrollPane(summaryPanel);
        res.setBorder(BorderFactory.createEmptyBorder());
        return res;
    }

    private static final class DQLCellRenderer extends CommonTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value instanceof ZonedDateTime zonedDateTime) {
                setText(zonedDateTime.format(DQLUtil.USER_FRIENDLY_DATE_FORMATTER));
            }
            return c;
        }
    }

    private static final class DQLHeaderRenderer extends CommonTableHeaderRenderer {
        private final Map<String, String> columnTypes;

        public DQLHeaderRenderer(Map<String, String> columnTypes, JTableHeader header) {
            super(header.getDefaultRenderer());
            this.columnTypes = columnTypes;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (c instanceof JLabel label) {
                String columnName = table.getColumnName(column);
                String columnType = columnTypes.get(columnName);
                JLabel cResult = label;
                if (label.getIcon() == null && StringUtil.isNotEmpty(columnType)) {
                    Icon icon = switch (columnType) {
                        case "long", "double", "number" -> DQLIcon.DQL_NUMBER;
                        case "array" -> DQLIcon.DQL_ARRAY;
                        case "record" -> DQLIcon.DQL_RECORD;
                        case "boolean" -> DQLIcon.DQL_BOOLEAN;
                        case "timestamp", "timeframe" -> DQLIcon.DQL_TIME_FIELD;
                        default -> DQLIcon.DQL_FIELD;
                    };
                    cResult = new JLabel(label.getText(), icon, JLabel.LEFT);
                    cResult.setToolTipText(columnType);
                }
                cResult.setHorizontalAlignment(SwingConstants.CENTER);
                return cResult;
            }
            return c;
        }
    }

    private void openSelectedCell(JTable table) {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if (row != -1 && col != -1) {
            Object value = table.getValueAt(row, col);
            FileEditorManager.getInstance(project).openFile(new DQLRecordFieldVirtualFile(
                    DQLBundle.message("components.tableResults.cellDetails.title", table.getColumnName(col))
                    , value == null ? "" : value,
                    columnTypes.get(table.getColumnName(col))
            ), true);
        }
    }

    private void openSelectedRow(JTable table) {
        int row = table.getSelectedRow();

        Map<String, Object> rowValues = new HashMap<>();
        for (int col = 0; col < table.getColumnCount(); col++) {
            rowValues.put(table.getColumnName(col), DQLComponentUtils.prepareColumnValue(table.getValueAt(row, col)));
        }
        FileEditorManager.getInstance(project).openFile(new DQLRecordVirtualFile(
                DQLBundle.message("components.dqlRecordDetails.fileName"),
                rowValues
        ), true);
    }
}

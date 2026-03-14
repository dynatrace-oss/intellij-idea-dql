package pl.thedeem.intellij.dql.exec.panel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.ComponentsUtils;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.components.PanelWithToolbarActions;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.components.table.CommonTable;
import pl.thedeem.intellij.common.components.table.RowCountTable;
import pl.thedeem.intellij.common.components.table.paging.PagingRowSorter;
import pl.thedeem.intellij.common.components.table.paging.TablePagingActions;
import pl.thedeem.intellij.common.components.table.rendering.CommonTableCellRenderer;
import pl.thedeem.intellij.common.components.table.rendering.CommonTableHeaderRenderer;
import pl.thedeem.intellij.common.sdk.model.DQLRecord;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.fileProviders.DQLRecordFieldVirtualFile;
import pl.thedeem.intellij.dql.fileProviders.DQLRecordVirtualFile;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DQLTableResultPanel extends BorderLayoutPanel implements PanelWithToolbarActions {
    protected final static ObjectMapper mapper = JsonMapper.builder().build();

    private final DQLResult result;
    protected CommonTable table = null;
    protected PagingRowSorter sorter = null;
    protected final List<TableFilter> filters = new ArrayList<>();

    public DQLTableResultPanel(@Nullable DQLResult result, @NotNull Project project) {
        super();
        withBorder(JBUI.Borders.empty()).andTransparent();
        this.result = result;
        if (result == null || result.getRecords() == null || result.getRecords().isEmpty()) {
            addToCenter(new TransparentScrollPane(new InformationComponent(
                    DQLBundle.message("components.results.table.information.noRecords"),
                    AllIcons.General.Information
            )));
        } else {
            this.table = createTableComponent(project);
            this.sorter = PagingRowSorter.install(table, 1000);
            addToCenter(createTableView(project));
        }
    }

    public void showColumnSettingsPopup(@NotNull AnActionEvent e) {
        if (table == null || result == null) {
            return;
        }
        JBPopup popup = table.createColumnsReorderPopup(result.getColumns());
        Component c = e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        if (e.getInputEvent() != null && e.getInputEvent().getComponent() != null) {
            c = e.getInputEvent().getComponent();
        }
        if (c != null) {
            popup.showUnderneathOf(c);
        }
    }

    private @NotNull CommonTable createTableComponent(@NotNull Project project) {
        Set<String> columns = result.getColumns();
        Map<String, String> columnTypes = result.getColumnTypes();
        List<ColumnInfo<DQLRecord, Object>> columnInfos = calculateColumns(columns, columnTypes);
        CommonTable table = new CommonTable(new ListTableModel<>(columnInfos.toArray(new ColumnInfo[]{}), new ArrayList<>(result.getRecords()), 0));
        table.addRightClickSelection();
        table.setDefaultRenderer(Object.class, new DQLCellRenderer());
        table.setDefaultRenderer(Boolean.class, new NullableBooleanRenderer());
        table.getTableHeader().setDefaultRenderer(new DQLHeaderRenderer(columnTypes, table.getTableHeader()));
        updateColumnSizes(table, columnTypes);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    JTable target = (JTable) e.getSource();
                    openSelectedCell(target, columnTypes, project);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showCellContextMenu(e, table, columnTypes, project);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showCellContextMenu(e, table, columnTypes, project);
                }
            }
        });

        return table;
    }

    private @NotNull JComponent createTableView(@NotNull Project project) {
        JTable recordNumberTable = new RowCountTable(table);
        TransparentScrollPane scrollPane = new TransparentScrollPane(table);
        scrollPane.setRowHeaderView(recordNumberTable);
        scrollPane.getRowHeader().setPreferredSize(recordNumberTable.getPreferredScrollableViewportSize());

        recordNumberTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    openSelectedRow(table, project);
                }
            }
        });
        return scrollPane;
    }

    private void updateColumnSizes(@NotNull CommonTable table, @NotNull Map<String, String> columnTypes) {
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

    private List<ColumnInfo<DQLRecord, Object>> calculateColumns(@NotNull Set<String> columns, @NotNull Map<String, String> columnTypes) {
        return columns.stream().map(s -> new ColumnInfo<DQLRecord, Object>(s) {
            @Override
            public @Nullable Object valueOf(DQLRecord record) {
                Object value = record.get(s);
                if (value == null) return null;
                String stringValue = value.toString();
                String columnValue = columnTypes.get(s);
                try {
                    return switch (columnValue) {
                        case "double", "number", "long" -> Double.valueOf(stringValue);
                        case "boolean" -> stringValue == null ? null : Boolean.valueOf(stringValue);
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

    @Override
    public @NotNull AnAction[] getToolbarActions() {
        List<AnAction> actions = new ArrayList<>();
        actions.add(new AnAction(DQLBundle.message("components.executionResult.actions.changeColumnsList.title"), null, AllIcons.Actions.PreviewDetails) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                showColumnSettingsPopup(e);
            }
        });
        if (sorter != null) {
            actions.add(new ToggleAction(DQLBundle.message("components.results.table.filter.action.title"), null, AllIcons.General.Filter) {
                @Override
                public boolean isSelected(@NotNull AnActionEvent e) {
                    return sorter.isFilterActive();
                }

                @Override
                public void setSelected(@NotNull AnActionEvent e, boolean selected) {
                    DefaultTableModel tableModel = createFilterTableModel();
                    JBTable filterTable = createFilterEditorTable(tableModel);

                    JPanel panel = ToolbarDecorator.createDecorator(filterTable)
                            .setAddAction(button -> {
                                tableModel.addRow(new Object[]{"=", ""});
                                int newRow = tableModel.getRowCount() - 1;
                                filterTable.setRowSelectionInterval(newRow, newRow);
                                filterTable.editCellAt(newRow, 1);
                                Component editorComp = filterTable.getEditorComponent();
                                if (editorComp != null) {
                                    editorComp.requestFocusInWindow();
                                }
                            })
                            .setRemoveAction(button -> {
                                if (filterTable.isEditing()) {
                                    filterTable.getCellEditor().cancelCellEditing();
                                }
                                int[] rows = filterTable.getSelectedRows();
                                for (int i = rows.length - 1; i >= 0; i--) {
                                    tableModel.removeRow(rows[i]);
                                }
                            })
                            .addExtraAction(new AnAction(DQLBundle.message("components.results.table.filter.action.clearAll"), null, AllIcons.Actions.GC) {
                                @Override
                                public void actionPerformed(@NotNull AnActionEvent e) {
                                    if (filterTable.isEditing()) {
                                        filterTable.getCellEditor().cancelCellEditing();
                                    }
                                    tableModel.setRowCount(0);
                                }
                            })
                            .disableUpDownActions()
                            .createPanel();
                    panel.setBorder(JBUI.Borders.empty(5));

                    tableModel.addTableModelListener(ev -> syncAndApplyFilters(tableModel));

                    JBPopup popup = JBPopupFactory.getInstance()
                            .createComponentPopupBuilder(panel, filterTable)
                            .setRequestFocus(true)
                            .setResizable(true)
                            .setMovable(true)
                            .setCancelOnOtherWindowOpen(false)
                            .createPopup();


                    Component component = e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
                    if (e.getInputEvent() != null && e.getInputEvent().getComponent() != null) {
                        component = e.getInputEvent().getComponent();
                    }
                    if (component != null) {
                        popup.showUnderneathOf(component);
                    } else {
                        popup.showInFocusCenter();
                    }
                }

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }
            });
            actions.add(new TablePagingActions(sorter));
        }
        return actions.toArray(new AnAction[]{});
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

    private static final class NullableBooleanRenderer extends CommonTableCellRenderer {
        private final TableCellRenderer delegate = new JBTable().getDefaultRenderer(Boolean.class);
        private final JBLabel empty = new JBLabel();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component original = delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value == null && original instanceof JComponent component) {
                empty.setOpaque(component.isOpaque());
                empty.setBackground(component.getBackground());
                empty.setForeground(component.getForeground());
                empty.setBorder(component.getBorder());
                empty.setText("");
                empty.setIcon(null);
                return empty;
            }
            return original;
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

    private void showCellContextMenu(@NotNull MouseEvent e, @NotNull JTable table, @NotNull Map<String, String> columnTypes, @NotNull Project project) {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if (row == -1 || col == -1) {
            return;
        }
        String cellValueString = Objects.requireNonNullElse(ComponentsUtils.prepareColumnValue(table.getValueAt(row, col)), "").toString();
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(createEDTAction(
                DQLBundle.message("components.results.table.contextMenu.copyCell"),
                DQLBundle.message("components.results.table.contextMenu.copyCell.description"),
                AllIcons.Actions.Copy,
                () -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(cellValueString), null)
        ));
        group.add(createEDTAction(
                DQLBundle.message("components.results.table.contextMenu.copyRow"),
                DQLBundle.message("components.results.table.contextMenu.copyRow.description"),
                AllIcons.Actions.Copy,
                () -> Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(Objects.requireNonNullElse(ComponentsUtils.getJsonContentForRows(table, table.getSelectedRows()), "")),
                        null
                )
        ));
        group.addSeparator();
        group.add(createEDTAction(
                DQLBundle.message("components.results.table.contextMenu.openCell"),
                DQLBundle.message("components.results.table.contextMenu.openCell.description"),
                AllIcons.Actions.MoveTo2,
                () -> openSelectedCell(table, columnTypes, project)
        ));
        group.add(createEDTAction(
                DQLBundle.message("components.results.table.contextMenu.openRow"),
                DQLBundle.message("components.results.table.contextMenu.openRow.description"),
                AllIcons.Actions.MoveTo2,
                () -> openSelectedRow(table, project)
        ));
        if (sorter != null) {
            group.addSeparator();
            group.add(createEDTAction(
                    DQLBundle.message("components.results.table.contextMenu.filterValue"),
                    DQLBundle.message("components.results.table.contextMenu.filterValue.description"),
                    AllIcons.General.Filter,
                    () -> {
                        filters.add(new TableFilter(true, cellValueString));
                        sorter.setFilter(buildFilter());
                    }
            ));
            group.add(createEDTAction(
                    DQLBundle.message("components.results.table.contextMenu.filterOutValue"),
                    DQLBundle.message("components.results.table.contextMenu.filterOutValue.description"),
                    AllIcons.General.Filter,
                    () -> {
                        filters.add(new TableFilter(false, cellValueString));
                        sorter.setFilter(buildFilter());
                    }
            ));
        }

        ActionPopupMenu popupMenu = ActionManager.getInstance()
                .createActionPopupMenu("DQL.TableCellContextMenu", group);
        popupMenu.getComponent().show(table, e.getX(), e.getY());
    }

    private void openSelectedCell(@NotNull JTable table, @NotNull Map<String, String> columnTypes, @NotNull Project project) {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();
        if (row != -1 && col != -1) {
            Object value = table.getValueAt(row, col);
            FileEditorManager.getInstance(project).openFile(new DQLRecordFieldVirtualFile(
                    DQLBundle.message("components.results.table.actions.openField.tabTitle", table.getColumnName(col)),
                    value == null ? "" : value,
                    columnTypes.get(table.getColumnName(col))
            ), true);
        }
    }

    private void openSelectedRow(@NotNull JTable table, @NotNull Project project) {
        int row = table.getSelectedRow();

        Map<String, Object> rowValues = new HashMap<>();
        for (int col = 0; col < table.getColumnCount(); col++) {
            rowValues.put(table.getColumnName(col), ComponentsUtils.prepareColumnValue(table.getValueAt(row, col)));
        }
        FileEditorManager.getInstance(project).openFile(new DQLRecordVirtualFile(
                DQLBundle.message("components.results.table.actions.openRecord.tabTitle"),
                rowValues
        ), true);
    }

    private @NotNull AnAction createEDTAction(@NotNull String title, @Nullable String description, @Nullable Icon icon, @NotNull Runnable action) {
        return new AnAction(title, description, icon) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                action.run();
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
        };
    }

    private @NotNull DefaultTableModel createFilterTableModel() {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"", ""}, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return String.class;
            }
        };
        for (TableFilter f : filters) {
            model.addRow(new Object[]{f.include() ? "=" : "!=", f.text()});
        }
        return model;
    }

    private @NotNull JBTable createFilterEditorTable(@NotNull DefaultTableModel model) {
        JBTable filterTable = new JBTable(model);
        filterTable.setVisibleRowCount(5);
        filterTable.setTableHeader(null);
        filterTable.setRowSelectionAllowed(true);
        filterTable.setColumnSelectionAllowed(false);
        TableColumn typeCol = filterTable.getColumnModel().getColumn(0);
        typeCol.setMinWidth(JBUI.scale(50));
        typeCol.setMaxWidth(JBUI.scale(50));
        typeCol.setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"=", "!="})));
        TableColumn filterCol = filterTable.getColumnModel().getColumn(1);
        filterCol.setPreferredWidth(JBUI.scale(250));

        return filterTable;
    }

    private void syncAndApplyFilters(@NotNull DefaultTableModel model) {
        filters.clear();
        for (int i = 0; i < model.getRowCount(); i++) {
            String type = Objects.toString(model.getValueAt(i, 0), "=");
            String text = Objects.toString(model.getValueAt(i, 1), "").trim();
            if (!text.isBlank()) {
                filters.add(new TableFilter("=".equals(type), text));
            }
        }
        if (sorter != null) {
            sorter.setFilter(buildFilter());
        }
    }

    private @Nullable RowFilter<Object, Object> buildFilter() {
        List<RowFilter<Object, Object>> rowFilters = new ArrayList<>();
        for (TableFilter filter : filters) {
            RowFilter<Object, Object> f = RowFilter.regexFilter("(?i)" + Pattern.quote(filter.text()));
            rowFilters.add(filter.include ? f : RowFilter.notFilter(f));
        }
        if (rowFilters.isEmpty()) {
            return null;
        }
        if (rowFilters.size() == 1) {
            return rowFilters.getFirst();
        }
        return RowFilter.andFilter(rowFilters);
    }

    public record TableFilter(boolean include, @NotNull String text) {
    }
}

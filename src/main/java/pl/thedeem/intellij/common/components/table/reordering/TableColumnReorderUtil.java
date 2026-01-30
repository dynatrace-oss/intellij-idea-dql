package pl.thedeem.intellij.common.components.table.reordering;

import com.intellij.ui.ListSpeedSearch;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.components.table.CommonTable;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

public class TableColumnReorderUtil {
    public @NotNull JComponent createColumnReorderingComponent(@NotNull CommonTable table, @NotNull Set<String> allColumns) {
        BorderLayoutPanel content = new BorderLayoutPanel();
        content.setBorder(JBUI.Borders.empty());
        content.addToCenter(new TransparentScrollPane(createColumnList(table, allColumns)));
        return content;
    }

    private @NotNull JBList<TableColumnItem> createColumnList(@NotNull CommonTable table, @NotNull Set<String> allColumns) {
        DefaultListModel<TableColumnItem> model = createTableModelList(table, allColumns);
        return createTableColumnList(model);
    }

    private @NotNull DefaultListModel<TableColumnItem> createTableModelList(@NotNull CommonTable table, @NotNull Set<String> allColumns) {
        LinkedHashMap<String, Boolean> initial = createInitialColumnState(table, allColumns);
        DefaultListModel<TableColumnItem> model = new DefaultListModel<>();
        for (Map.Entry<String, Boolean> it : initial.entrySet()) {
            model.addElement(new TableColumnItem(it.getKey(), it.getValue()));
        }
        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                applyColumnSettingsFromModel(table, model);
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                applyColumnSettingsFromModel(table, model);
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                applyColumnSettingsFromModel(table, model);
            }
        });
        return model;
    }

    private static @NotNull JBList<TableColumnItem> createTableColumnList(DefaultListModel<TableColumnItem> model) {
        JBList<TableColumnItem> list = new JBList<>(model);
        list.setCellRenderer(new TableColumnItemRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDragEnabled(true);
        list.setDropMode(DropMode.INSERT);
        list.setTransferHandler(new ReorderableListTransferHandler());
        list.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index < 0) {
                    list.setCursor(Cursor.getDefaultCursor());
                    return;
                }
                Rectangle r = list.getCellBounds(index, index);
                list.setCursor(r != null && r.contains(e.getPoint()) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                list.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                if (index < 0) return;
                Rectangle cellBounds = list.getCellBounds(index, index);
                if (cellBounds == null || !cellBounds.contains(e.getPoint())) return;

                TableColumnItem item = model.get(index);
                model.set(index, new TableColumnItem(item.name(), !item.visible()));
            }
        });
        ListSpeedSearch.installOn(list, TableColumnItem::name);
        return list;
    }

    private @NotNull LinkedHashMap<String, Boolean> createInitialColumnState(@NotNull CommonTable table, @NotNull Set<String> allColumns) {
        LinkedHashMap<String, Boolean> result = new LinkedHashMap<>();
        TableColumnModel columnModel = table.getColumnModel();
        Set<String> visible = new HashSet<>();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            String name = columnModel.getColumn(i).getHeaderValue().toString();
            visible.add(name);
            result.put(name, true);
        }
        List<String> remaining = allColumns.stream().filter(c -> !visible.contains(c)).sorted().toList();
        for (String c : remaining) {
            result.put(c, false);
        }
        return result;
    }

    private void applyColumnSettingsFromModel(@NotNull CommonTable table, @NotNull DefaultListModel<TableColumnItem> model) {
        List<TableColumnItem> items = Collections.list(model.elements());
        List<String> orderedVisible = items.stream().filter(TableColumnItem::visible).map(TableColumnItem::name).toList();

        TableColumnModel columnModel = table.getColumnModel();

        Map<String, TableColumn> byName = new HashMap<>();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn c = columnModel.getColumn(i);
            byName.put(c.getHeaderValue().toString(), c);
        }

        List<String> currentlyVisible = new ArrayList<>(byName.keySet());
        for (String name : currentlyVisible) {
            if (!orderedVisible.contains(name)) {
                TableColumn c = byName.get(name);
                if (c != null) {
                    columnModel.removeColumn(c);
                }
            }
        }

        byName.clear();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn c = columnModel.getColumn(i);
            byName.put(c.getHeaderValue().toString(), c);
        }

        Map<String, TableColumn> allKnownColumns = new HashMap<>(byName);

        for (String name : orderedVisible) {
            TableColumn existing = allKnownColumns.get(name);
            if (existing == null) {
                int modelIndex = resolveModelIndexByHeader(table, name);
                if (modelIndex >= 0) {
                    TableColumn newCol = new TableColumn(modelIndex);
                    newCol.setHeaderValue(name);
                    columnModel.addColumn(newCol);
                    allKnownColumns.put(name, newCol);
                }
            }
        }

        for (int target = 0; target < orderedVisible.size(); target++) {
            String name = orderedVisible.get(target);
            int from = findViewIndexByHeader(table, name);
            if (from >= 0 && from != target) {
                columnModel.moveColumn(from, target);
            }
        }
        table.revalidate();
        table.repaint();
    }

    private int findViewIndexByHeader(@NotNull JTable table, @NotNull String header) {
        TableColumnModel cm = table.getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
            Object hv = cm.getColumn(i).getHeaderValue();
            if (hv != null && header.equals(hv.toString())) return i;
        }
        return -1;
    }

    private int resolveModelIndexByHeader(@NotNull JTable table, @NotNull String header) {
        for (int modelIndex = 0; modelIndex < table.getModel().getColumnCount(); modelIndex++) {
            String modelName = table.getModel().getColumnName(modelIndex);
            if (header.equals(modelName)) return modelIndex;
        }
        return -1;
    }
}

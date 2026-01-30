package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.components.table.CommonTable;
import pl.thedeem.intellij.common.components.table.rendering.MultiLineCellRenderer;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DQLRecordPanel extends BorderLayoutPanel {
    public DQLRecordPanel(@NotNull Map<String, Object> record) {
        super();
        CommonTable table = new CommonTable(new ListTableModel<>(
                getColumnInfos().toArray(new ColumnInfo[]{}),
                record.entrySet().stream().toList(),
                0
        ));
        MultiLineCellRenderer.installOn(table);
        table.setDefaultRenderer(Object.class, new MultiLineCellRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setColumnPreferredWidthInCharacters(0, 30);
        addToCenter(new TransparentScrollPane(table));
    }

    private List<ColumnInfo<Map.Entry<String, Object>, Object>> getColumnInfos() {
        List<ColumnInfo<Map.Entry<String, Object>, Object>> columnInfos = new ArrayList<>(2);
        columnInfos.add(new ColumnInfo<>(DQLBundle.message("components.dqlRecordDetails.columns.field")) {
            @Override
            public @Nullable Object valueOf(Map.Entry<String, Object> s) {
                return s.getKey();
            }
        });
        columnInfos.add(new ColumnInfo<>(DQLBundle.message("components.dqlRecordDetails.columns.fieldValue")) {
            @Override
            public @Nullable Object valueOf(Map.Entry<String, Object> s) {
                return s.getValue();
            }
        });
        return columnInfos;
    }
}

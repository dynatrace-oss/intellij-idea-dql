package pl.thedeem.intellij.dql.components.execution;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.CommonTable;
import pl.thedeem.intellij.common.components.MultiLineCellRenderer;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DQLRecordPanel extends JPanel {
    public DQLRecordPanel(@NotNull Map<String, Object> record) {
        setLayout(new BorderLayout());
        CommonTable table = new CommonTable(new ListTableModel<>(
                getColumnInfos().toArray(new ColumnInfo[]{}),
                record.entrySet().stream().toList(),
                0
        ));
        MultiLineCellRenderer.installOn(table);
        table.setDefaultRenderer(Object.class, new MultiLineCellRenderer());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        JBScrollPane scroll = new JBScrollPane(table);
        table.setColumnPreferredWidthInCharacters(0, 30);
        add(scroll, BorderLayout.CENTER);
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

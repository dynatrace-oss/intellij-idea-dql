package pl.thedeem.intellij.dql.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.JBPopupMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DQLComponentUtils {
    protected final static ObjectMapper mapper = JsonMapper.builder().build();
    public static final Border DEFAULT_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    public static @NotNull JBPopupMenu createDefaultPopupMenu() {
        JBPopupMenu popupMenu = new JBPopupMenu();

        JMenuItem pasteAction = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteAction.setIcon(AllIcons.Actions.MenuPaste);
        pasteAction.setText(DQLBundle.message("components.popupMenu.actions.paste"));
        popupMenu.add(pasteAction);

        return popupMenu;
    }

    public static @Nullable String getJsonContentForRows(@NotNull JTable table, int startRow, int rowCount) {
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

    public static @NotNull JMenuItem createPopupMenuItem(@NotNull String text, @NotNull Icon icon, @NotNull ActionListener listener) {
        JMenuItem action = new JMenuItem(text);
        action.setIcon(icon);
        action.setBorder(DQLComponentUtils.DEFAULT_BORDER);
        action.addActionListener(listener);
        return action;
    }

    public static @Nullable Object prepareColumnValue(@Nullable Object value) {
        if (value instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.format(DQLUtil.DQL_DATE_FORMATTER);
        }
        return value;
    }

    public static void addCopyingCellValuesSupport(@NotNull JTable table) {
        addCopyingCellValuesSupport(table, table);
    }

    public static void addCopyingCellValuesSupport(@NotNull JTable table, @NotNull JTable copyFromTable) {
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        table.getInputMap().put(copy, "copy");
        table.getActionMap().put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = copyFromTable.getSelectedRow();
                int selectedRows = copyFromTable.getSelectedRowCount();
                int col = copyFromTable.getSelectedColumn();
                if (selectedRows > 1) {
                    String jsonContent = getJsonContentForRows(table, row, selectedRows);
                    StringSelection selection = new StringSelection(jsonContent != null ? jsonContent : "");
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                } else if (row != -1 && col != -1) {
                    Object value = prepareColumnValue(copyFromTable.getValueAt(row, col));
                    StringSelection selection = new StringSelection(value != null ? value.toString() : "");
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                }
            }
        });
    }
}


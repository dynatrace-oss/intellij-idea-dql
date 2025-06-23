package pl.thedeem.intellij.dql.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableResults<T> extends JBTable {
    public static final Border DEFAULT_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);

    protected final static ObjectMapper mapper = new ObjectMapper();

    public TableResults(ListTableModel<T> model) {
        super(model);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    public void configureCellsRendering() {
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (c instanceof JComponent jc && value != null) {
                    jc.setBorder(DEFAULT_BORDER);
                }
                return c;
            }
        });
    }

    public void setCopyCellValues() {
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        getInputMap().put(copy, "copy");
        getActionMap().put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = getSelectedRow();
                int col = getSelectedColumn();
                if (row != -1 && col != -1) {
                    Object value = getValueAt(row, col);
                    StringSelection selection = new StringSelection(value != null ? value.toString() : "");
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
                }
            }
        });
    }

    public void addContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(getOpenValueAction());
        popupMenu.addSeparator();
        popupMenu.add(getCopyRowAction());
        setComponentPopupMenu(popupMenu);
    }

    protected JMenuItem getCopyRowAction() {
        JMenuItem action = new JMenuItem(DQLBundle.message("components.tableResults.actions.copySelectedRows"));
        action.setIcon(AllIcons.General.Copy);
        action.setBorder(DEFAULT_BORDER);
        action.addActionListener(e -> {
            int row = getSelectedRow();
            int count = getSelectedRowCount();
            String content = getJsonContentForRows(row, count);
            if (StringUtil.isNotEmpty(content)) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(content), null);
            }
        });
        return action;
    }

    protected JMenuItem getOpenValueAction() {
        JMenuItem action = new JMenuItem(DQLBundle.message("components.tableResults.actions.openCellDetails"));
        action.setIcon(AllIcons.General.OpenInToolWindow);
        action.setBorder(DEFAULT_BORDER);
        action.addActionListener(e -> {
            int row = getSelectedRow();
            int col = getSelectedColumn();
            if (row != -1 && col != -1) {
                Object value = getValueAt(row, col);

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
                        DQLBundle.message("components.tableResults.cellDetails.title", getColumnName(col)),
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
        return action;
    }

    @Nullable
    public String getJsonContentForRows(int startRow, int rowCount) {
        if (startRow != -1) {
            List<Map<String, Object>> results = new ArrayList<>();
            for (int i = startRow; i < rowCount; i++) {
                Map<String, Object> rowValues = new HashMap<>();
                for (int col = 0; col < getColumnCount(); col++) {
                    rowValues.put(getColumnName(col), getValueAt(i, col));
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

    public void addSorting() {
        setAutoCreateRowSorter(true);
    }
}

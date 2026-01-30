package pl.thedeem.intellij.common.components.table.reordering;

import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;

import javax.swing.*;
import java.awt.*;

class TableColumnItemRenderer extends BorderLayoutPanel implements ListCellRenderer<TableColumnItem> {
    private final JBCheckBox checkBox = new JBCheckBox();
    private final JBLabel label = new JBLabel();

    public TableColumnItemRenderer() {
        setOpaque(true);
        setBorder(JBUI.Borders.empty(5));
        checkBox.setOpaque(false);
        label.setHorizontalAlignment(JLabel.LEFT);
        addToLeft(checkBox);
        addToCenter(label);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends TableColumnItem> list, TableColumnItem value, int index, boolean isSelected, boolean cellHasFocus) {
        checkBox.setSelected(value != null && value.visible());
        label.setText(value == null ? "" : value.name());

        Color bg = isSelected ? JBColor.background() : list.getBackground();
        Color fg = isSelected ? JBColor.foreground() : list.getForeground();

        setBackground(bg);
        label.setForeground(fg);
        return this;
    }
}

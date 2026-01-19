package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.ComponentsUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;

import javax.swing.*;

public abstract class AbstractTimeFieldAction extends AbstractTextFieldAction<String> {
    public AbstractTimeFieldAction(@Nullable String value, @NotNull String text, @Nullable String description, @Nullable Icon icon) {
        super(value, text, description, icon);
    }

    @Override
    protected @Nullable String validate() throws IllegalArgumentException {
        try {
            DQLUtil.parseUserTime(myField.getText().trim());
            return null;
        } catch (IllegalArgumentException ex) {
            return DQLBundle.message("components.queryRange.invalidDate", ex.getMessage());
        }
    }

    @Override
    public String getValue() {
        return myField.getText().trim();
    }

    @Override
    protected JBTextField createTextField() {
        JBTextField result = super.createTextField();
        JBPopupMenu popupMenu = new JBPopupMenu();
        JBMenuItem currentTimestampAction = new JBMenuItem(DQLBundle.message("components.queryExecution.actions.getCurrentTimestamp"));
        currentTimestampAction.setIcon(AllIcons.General.Inline_edit);
        currentTimestampAction.setBorder(ComponentsUtils.DEFAULT_BORDER);
        currentTimestampAction.addActionListener(e -> result.setText(DQLUtil.getCurrentTimeTimestamp()));
        popupMenu.add(currentTimestampAction);
        result.setComponentPopupMenu(popupMenu);
        return result;
    }
}

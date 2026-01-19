package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

public abstract class AbstractNumericFieldAction extends AbstractTextFieldAction<Long> {
    public AbstractNumericFieldAction(@Nullable Long value, @NotNull String text, @Nullable String description, @Nullable Icon icon) {
        super(value != null ? String.valueOf(value) : null, text, description, icon);
    }

    @Override
    protected JBTextField createTextField() {
        JBTextField result = super.createTextField();
        ((AbstractDocument) result.getDocument()).setDocumentFilter(new NumericFilter());
        return result;
    }

    private static class NumericFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (string.matches("-?\\d*")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (text.matches("-?\\d*")) {
                super.replace(fb, offset, length, text, attrs);
            }
        }
    }

    @Override
    public Long getValue() {
        String text = myField.getText();
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}

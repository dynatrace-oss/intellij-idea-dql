package pl.thedeem.intellij.common.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;

import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class ResizableTextField extends JBTextField {
    private final static int TEXT_FIELD_MIN_WIDTH = 75;
    private final static int TEXT_FIELD_MAX_WIDTH = 400;

    public ResizableTextField() {
        getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                resizeToFit();
            }
        });
    }

    public void resizeToFit() {
        String text = getText();
        FontMetrics fm = getFontMetrics(getFont());

        int textWidth = fm.stringWidth(text + "WW");
        int newWidth = Math.max(JBUI.scale(TEXT_FIELD_MIN_WIDTH), Math.min(JBUI.scale(TEXT_FIELD_MAX_WIDTH), textWidth));

        Dimension dim = new Dimension(newWidth, getPreferredSize().height);
        setPreferredSize(dim);
        setMinimumSize(dim);

        Container parent = getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }

    public static @NotNull ResizableTextField createStandardField(@Nullable String placeholder, @Nullable String tooltip) {
        ResizableTextField result = new ResizableTextField();
        result.setToolTipText(tooltip);
        result.getEmptyText().setText(placeholder);
        result.setPreferredSize(new Dimension(result.getPreferredSize().width, JBUI.scale(25)));
        result.resizeToFit();
        return result;
    }

    public static @NotNull ResizableTextField createTimeField(@Nullable String placeholder, @Nullable String tooltip) {
        ResizableTextField result = createStandardField(placeholder, tooltip);
        Border defaultBorder = result.getBorder();
        result.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                String error = validateTime(result.getText());
                if (error != null) {
                    result.setToolTipText(error);
                    result.setBorder(JBUI.Borders.compound(JBUI.Borders.customLineBottom(JBColor.RED), defaultBorder));
                } else {
                    result.setToolTipText(tooltip);
                    result.setBorder(defaultBorder);
                }
                result.revalidate();
                result.repaint();
            }
        });

        JBPopupMenu popupMenu = new JBPopupMenu();
        JBMenuItem currentTimestampAction = new JBMenuItem(DQLBundle.message("components.queryExecution.actions.getCurrentTimestamp"));
        currentTimestampAction.setIcon(AllIcons.General.Inline_edit);
        currentTimestampAction.setBorder(ComponentsUtils.DEFAULT_BORDER);
        currentTimestampAction.addActionListener(e -> result.setText(DQLUtil.getCurrentTimeTimestamp()));
        popupMenu.add(currentTimestampAction);
        result.setComponentPopupMenu(popupMenu);
        return result;
    }

    public static @NotNull ResizableTextField createNumericField(@Nullable String placeholder, @Nullable String tooltip) {
        ResizableTextField result = createStandardField(placeholder, tooltip);
        ((AbstractDocument) result.getDocument()).setDocumentFilter(new NumericFilter());
        return result;
    }

    private static @Nullable String validateTime(@NotNull String time) throws IllegalArgumentException {
        try {
            DQLUtil.parseUserTime(time.trim());
            return null;
        } catch (IllegalArgumentException ex) {
            return DQLBundle.message("components.queryRange.invalidDate", ex.getMessage());
        }
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
}

package pl.thedeem.intellij.dql.actions.executionToolbar;

import com.intellij.icons.AllIcons;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public abstract class AbstractTextFieldAction<T> extends AnAction implements CustomComponentAction, DumbAware {
    private final static int TEXT_FIELD_MIN_WIDTH = 75;
    private final static int TEXT_FIELD_MAX_WIDTH = 400;
    private final Icon myIcon;
    private final String myDescription;
    private final String myText;
    protected JBTextField myField;
    protected JBLabel myLabel;
    protected String error;

    public AbstractTextFieldAction(@Nullable String value, @NotNull String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
        this.myIcon = icon;
        this.myDescription = description;
        this.myText = text;
        this.myField = createTextField();
        this.myLabel = new JBLabel(this.myIcon);
        this.myField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                resizeToFit();
                error = validate();
                ApplicationManager.getApplication().invokeLater(() -> {
                    AnActionEvent event = AnActionEvent.createEvent(
                            DataManager.getInstance().getDataContext(myField),
                            getTemplatePresentation().clone(),
                            ActionPlaces.UNKNOWN,
                            ActionUiKind.NONE,
                            null
                    );
                    ActionUtil.performActionDumbAwareWithCallbacks(AbstractTextFieldAction.this, event);
                });
            }
        });
        if (value != null) {
            this.myField.setText(value);
        }
        resizeToFit();
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        this.myLabel.setOpaque(false);
        this.myLabel.setBorder(BorderFactory.createEmptyBorder());
        this.myField.setOpaque(false);
        resetView();
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.setBorder(JBUI.Borders.empty());
        panel.add(this.myLabel, BorderLayout.WEST);
        panel.add(this.myField, BorderLayout.CENTER);
        this.myField.setPreferredSize(new Dimension(this.myField.getPreferredSize().width, JBUI.scale(25)));
        return panel;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        e.getPresentation().setEnabledAndVisible(isVisible(e));
        if (error != null) {
            showError(error);
        } else {
            resetView();
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    protected void resetView() {
        this.myLabel.setToolTipText(this.myDescription);
        this.myLabel.setIcon(this.myIcon);
        this.myField.setToolTipText(this.myDescription);
        myLabel.repaint();
        myField.repaint();
    }

    protected void showError(@NotNull String message) {
        this.myLabel.setToolTipText(message);
        this.myLabel.setIcon(AllIcons.General.Error);
        this.myField.setToolTipText(message);
        myLabel.repaint();
        myField.repaint();
    }

    public abstract T getValue();

    protected JBTextField createTextField() {
        JBTextField result = new JBTextField();
        result.getEmptyText().setText(this.myText);
        return result;
    }

    protected @Nullable String validate() throws IllegalArgumentException {
        return null;
    }

    protected boolean isVisible(@NotNull AnActionEvent e) {
        return true;
    }

    private void resizeToFit() {
        String text = myField.getText();
        FontMetrics fm = myField.getFontMetrics(myField.getFont());

        int textWidth = fm.stringWidth(text + "WW");
        int newWidth = Math.max(JBUI.scale(TEXT_FIELD_MIN_WIDTH), Math.min(JBUI.scale(TEXT_FIELD_MAX_WIDTH), textWidth));

        Dimension dim = new Dimension(newWidth, myField.getPreferredSize().height);
        myField.setPreferredSize(dim);
        myField.setMinimumSize(dim);

        Container parent = myField.getParent();
        if (parent != null) {
            parent.revalidate();
            parent.repaint();
        }
    }
}

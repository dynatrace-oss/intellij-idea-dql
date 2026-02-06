package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.components.QueryTimeframeConfigurationComponent;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Objects;

public class QueryConfigurationTimeframeAction extends AnAction implements CustomComponentAction {
    private final QueryTimeframeConfigurationComponent myQueryTimeframeComponent;

    private JComponent component;

    public QueryConfigurationTimeframeAction() {
        myQueryTimeframeComponent = new QueryTimeframeConfigurationComponent();
        installActionEvent(myQueryTimeframeComponent.queryStartField());
        installActionEvent(myQueryTimeframeComponent.queryEndField());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        if (configuration == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        e.getPresentation().putClientProperty(DQLQueryConfigurationService.QUERY_CONFIGURATION, configuration);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        if (configuration != null) {
            configuration.setTimeframeStart(myQueryTimeframeComponent.queryStartField().getText());
            configuration.setTimeframeEnd(myQueryTimeframeComponent.queryEndField().getText());
        }
    }

    @Override
    public void updateCustomComponent(@NotNull JComponent component, @NotNull Presentation presentation) {
        QueryConfiguration config = presentation.getClientProperty(DQLQueryConfigurationService.QUERY_CONFIGURATION);
        if (config != null) {
            String currentStart = Objects.requireNonNullElse(config.timeframeStart(), "");
            if (!myQueryTimeframeComponent.queryStartField().isFocusOwner()
                    && !Objects.equals(myQueryTimeframeComponent.queryStartField().getText(), currentStart)) {
                myQueryTimeframeComponent.queryStartField().setText(currentStart);
            }
            String currentEnd = Objects.requireNonNullElse(config.timeframeEnd(), "");
            if (!myQueryTimeframeComponent.queryEndField().isFocusOwner()
                    && !Objects.equals(myQueryTimeframeComponent.queryEndField().getText(), currentEnd)) {
                myQueryTimeframeComponent.queryEndField().setText(currentEnd);
            }
        }
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        if (component == null) {
            component = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            component.setOpaque(false);
            component.setBorder(JBUI.Borders.empty());
            component.add(myQueryTimeframeComponent);
        }
        return component;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    public JComponent component() {
        return component;
    }

    private void installActionEvent(@NotNull JBTextField component) {
        component.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                ApplicationManager.getApplication().invokeLater(() -> ActionManager.getInstance().tryToExecute(
                        QueryConfigurationTimeframeAction.this,
                        null,
                        component,
                        ActionPlaces.UNKNOWN,
                        true
                ));
            }
        });
    }
}

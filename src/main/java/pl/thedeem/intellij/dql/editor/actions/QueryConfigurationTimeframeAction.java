package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.components.QueryTimeframeConfigurationComponent;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.query.model.QueryConfiguration;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Objects;

public class QueryConfigurationTimeframeAction extends AnAction implements CustomComponentAction {
    private final QueryTimeframeConfigurationComponent myQueryTimeframeComponent;

    private JComponent component;

    public QueryConfigurationTimeframeAction() {
        myQueryTimeframeComponent = new QueryTimeframeConfigurationComponent()
                .configureFields((field) -> field.setPreferredSize(new Dimension(field.getPreferredSize().width, JBUI.scale(25))));
        myQueryTimeframeComponent.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 2));
        installActionEvent(myQueryTimeframeComponent.queryStartField());
        installActionEvent(myQueryTimeframeComponent.queryEndField());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DataContext ctx = e.getDataContext();
        if (ctx.getData(DQLQueryConfigurationService.DATA_ORIGINAL_FILE) == null
                && ctx.getData(DQLQueryConfigurationService.DATA_TENANT) == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        e.getPresentation().putClientProperty(DQLQueryConfigurationService.TIMEFRAME_START,
                ctx.getData(DQLQueryConfigurationService.DATA_TIMEFRAME_START));
        e.getPresentation().putClientProperty(DQLQueryConfigurationService.TIMEFRAME_END,
                ctx.getData(DQLQueryConfigurationService.DATA_TIMEFRAME_END));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file != null) {
            DQLQueryConfigurationService service = DQLQueryConfigurationService.getInstance();
            QueryConfiguration configuration = service.getQueryConfiguration(file);
            configuration.setTimeframeStart(myQueryTimeframeComponent.queryStartField().getText());
            configuration.setTimeframeEnd(myQueryTimeframeComponent.queryEndField().getText());
            service.updateConfiguration(file, configuration);
        }
    }

    @Override
    public void updateCustomComponent(@NotNull JComponent component, @NotNull Presentation presentation) {
        String currentStart = Objects.requireNonNullElse(
                presentation.getClientProperty(DQLQueryConfigurationService.TIMEFRAME_START), "");
        if (!myQueryTimeframeComponent.queryStartField().isFocusOwner()
                && !Objects.equals(myQueryTimeframeComponent.queryStartField().getText(), currentStart)) {
            myQueryTimeframeComponent.queryStartField().setText(currentStart);
        }
        String currentEnd = Objects.requireNonNullElse(
                presentation.getClientProperty(DQLQueryConfigurationService.TIMEFRAME_END), "");
        if (!myQueryTimeframeComponent.queryEndField().isFocusOwner()
                && !Objects.equals(myQueryTimeframeComponent.queryEndField().getText(), currentEnd)) {
            myQueryTimeframeComponent.queryEndField().setText(currentEnd);
        }
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        if (component == null) {
            component = new JBPanel<>(new FlowLayout(FlowLayout.LEFT, 0, 3))
                    .withBorder(JBUI.Borders.empty())
                    .andTransparent();
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

package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.components.QueryConfigurationComponent;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.Objects;

public class QueryConfigurationOptionsAction extends AnAction implements CustomComponentAction {
    public static final DataKey<Boolean> SHOW_OPTIONS_INITIALLY = DataKey.create("DQL_SHOW_OPTIONS_INITIALLY");
    public static final Key<Boolean> SHOW_OPTIONS_INITIALLY_KEY = Key.create("DQL_SHOW_OPTIONS_INITIALLY");
    public static final DataKey<Boolean> SHOW_OPTIONS_BUTTON = DataKey.create("DQL_SHOW_OPTIONS_BUTTON");

    private final QueryConfigurationComponent myQueryConfigurationComponent;

    private Boolean optionsHidden = true;
    private JComponent component;

    public QueryConfigurationOptionsAction() {
        myQueryConfigurationComponent = new QueryConfigurationComponent();
        installActionEvent(myQueryConfigurationComponent.scanLimit());
        installActionEvent(myQueryConfigurationComponent.maxBytes());
        installActionEvent(myQueryConfigurationComponent.maxRecords());
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        if (configuration == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        e.getPresentation().putClientProperty(DQLQueryConfigurationService.QUERY_CONFIGURATION, configuration);
        e.getPresentation().putClientProperty(SHOW_OPTIONS_INITIALLY_KEY, e.getData(SHOW_OPTIONS_INITIALLY));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        if (configuration != null) {
            configuration.setDefaultScanLimit(parseLong(myQueryConfigurationComponent.scanLimit().getText()));
            configuration.setMaxResultBytes(parseLong(myQueryConfigurationComponent.maxBytes().getText()));
            configuration.setMaxResultRecords(parseLong(myQueryConfigurationComponent.maxRecords().getText()));
        }
    }

    @Override
    public void updateCustomComponent(@NotNull JComponent component, @NotNull Presentation presentation) {
        myQueryConfigurationComponent.setVisible(!Boolean.TRUE.equals(optionsHidden));
        QueryConfiguration config = presentation.getClientProperty(DQLQueryConfigurationService.QUERY_CONFIGURATION);
        if (config != null) {
            if (!myQueryConfigurationComponent.scanLimit().isFocusOwner()
                    && !Objects.equals(parseLong(myQueryConfigurationComponent.scanLimit().getText()), config.defaultScanLimit())) {
                myQueryConfigurationComponent.scanLimit().setText(String.valueOf(config.defaultScanLimit()));
            }
            if (!myQueryConfigurationComponent.maxBytes().isFocusOwner()
                    && !Objects.equals(parseLong(myQueryConfigurationComponent.maxBytes().getText()), config.maxResultBytes())) {
                myQueryConfigurationComponent.maxBytes().setText(String.valueOf(config.maxResultRecords()));
            }
            if (!myQueryConfigurationComponent.maxRecords().isFocusOwner()
                    && !Objects.equals(parseLong(myQueryConfigurationComponent.maxRecords().getText()), config.maxResultRecords())) {
                myQueryConfigurationComponent.maxRecords().setText(String.valueOf(config.maxResultRecords()));
            }
        }
        component.revalidate();
        component.repaint();
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        if (component == null) {
            component = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            component.setOpaque(false);
            component.setBorder(JBUI.Borders.empty());
            DefaultActionGroup group = new DefaultActionGroup();

            group.addAction(new ToggleAction(
                    DQLBundle.message("action.DQLExecutionManagerToolbar.moreOptions"),
                    null,
                    AllIcons.Actions.ToggleVisibility
            ) {
                @Override
                public boolean isSelected(@NotNull AnActionEvent anActionEvent) {
                    if (optionsHidden == null) {
                        optionsHidden = !Boolean.TRUE.equals(anActionEvent.getData(SHOW_OPTIONS_INITIALLY));
                    }
                    return !optionsHidden;
                }

                @Override
                public void setSelected(@NotNull AnActionEvent e, boolean selected) {
                    optionsHidden = !selected;
                    myQueryConfigurationComponent.setVisible(!optionsHidden);
                    ActivityTracker.getInstance().inc();
                }

                @Override
                public void update(@NotNull AnActionEvent e) {
                    super.update(e);
                    e.getPresentation().setEnabledAndVisible(!Boolean.FALSE.equals(e.getData(SHOW_OPTIONS_BUTTON)));
                }

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }
            });

            ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DQL.QueryConfigurationOptionsAction", group, true);
            toolbar.setTargetComponent(component);
            toolbar.getComponent().setBorder(JBUI.Borders.empty());
            toolbar.getComponent().setOpaque(false);
            component.add(toolbar.getComponent());
            component.add(myQueryConfigurationComponent);
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

    private @Nullable Long parseLong(@Nullable String text) {
        if (StringUtil.isEmpty(text)) {
            return null;
        }
        try {
            return Long.valueOf(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void installActionEvent(@NotNull JBTextField component) {
        component.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                ApplicationManager.getApplication().invokeLater(() -> ActionManager.getInstance().tryToExecute(
                        QueryConfigurationOptionsAction.this,
                        null,
                        component,
                        ActionPlaces.UNKNOWN,
                        true
                ));
            }
        });
    }
}

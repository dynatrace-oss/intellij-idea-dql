package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.psi.PsiFile;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import javax.swing.*;
import java.awt.*;

public class QueryConfigurationAction extends AnAction implements CustomComponentAction {
    public static final DataKey<Boolean> SHOW_TIMEFRAME = DataKey.create("DQL_SHOW_TIMEFRAME_OPTIONS");
    public static final DataKey<Boolean> SHOW_CONFIGURATION = DataKey.create("DQL_SHOW_CONFIGURATION_OPTIONS");
    public static final DataKey<Boolean> SHOW_QUERY_EXECUTE_BUTTON = DataKey.create("DQL_SHOW_QUERY_EXECUTE_BUTTON");
    public static final DataKey<Boolean> SHOW_TENANT_SELECTION = DataKey.create("DQL_SHOW_TENANT_SELECTION");
    public static final DataKey<Boolean> SHOW_RUN_CONFIG_CREATOR = DataKey.create("DQL_SHOW_RUN_CONFIG_CREATOR");

    private JComponent component;

    @Override
    public void update(@NotNull AnActionEvent e) {
        if (e.isFromContextMenu()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        if (configuration == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        e.getPresentation().putClientProperty(DQLQueryConfigurationService.QUERY_CONFIGURATION, configuration);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        if (configuration != null && file != null) {
            DQLQueryConfigurationService.getInstance().updateConfiguration(file, configuration);
        }
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        if (component == null) {
            component = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 3));
            component.setOpaque(false);
            component.setBorder(JBUI.Borders.empty());
            DefaultActionGroup group = new DefaultActionGroup();
            group.add(new TenantSelectorAction() {
                @Override
                protected void updateSelectedTenant(@NotNull String selectedTenant, @NotNull AnActionEvent e) {
                    super.updateSelectedTenant(selectedTenant, e);
                    QueryConfigurationAction.this.actionPerformed(e);
                }

                @Override
                public void update(@NotNull AnActionEvent e) {
                    super.update(e);
                    e.getPresentation().setEnabledAndVisible(!Boolean.FALSE.equals(e.getData(SHOW_TENANT_SELECTION)));
                }
            });
            group.add(new StartStopDQLExecutionAction() {
                @Override
                public void update(@NotNull AnActionEvent e) {
                    super.update(e);
                    e.getPresentation().setEnabledAndVisible(!Boolean.FALSE.equals(e.getData(SHOW_QUERY_EXECUTE_BUTTON)));
                }
            });
            group.addAction(new QueryConfigurationTimeframeAction() {
                @Override
                public void update(@NotNull AnActionEvent e) {
                    super.update(e);
                    e.getPresentation().setEnabledAndVisible(!Boolean.FALSE.equals(e.getData(SHOW_TIMEFRAME)));
                }

                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    super.actionPerformed(e);
                    QueryConfigurationAction.this.actionPerformed(e);
                }
            });
            group.addAction(new QueryConfigurationOptionsAction() {
                @Override
                public void update(@NotNull AnActionEvent e) {
                    super.update(e);
                    e.getPresentation().setEnabledAndVisible(!Boolean.FALSE.equals(e.getData(SHOW_CONFIGURATION)));
                }

                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    super.actionPerformed(e);
                    QueryConfigurationAction.this.actionPerformed(e);
                }
            });
            group.add(new SaveQueryConfigurationAction() {
                @Override
                public void update(@NotNull AnActionEvent e) {
                    super.update(e);
                    e.getPresentation().setEnabledAndVisible(!Boolean.FALSE.equals(e.getData(SHOW_RUN_CONFIG_CREATOR)));
                }
            });
            ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("DQL.QueryConfigurationAction", group, true);
            toolbar.setTargetComponent(component);
            toolbar.getComponent().setBorder(JBUI.Borders.empty());
            toolbar.getComponent().setOpaque(false);
            component.add(toolbar.getComponent());
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
}

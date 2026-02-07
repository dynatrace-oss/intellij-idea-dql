package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

public class TenantSelectorAction extends ComboBoxAction {
    public TenantSelectorAction() {
        super();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        if (project == null) {
            presentation.setVisible(false);
            return;
        }
        presentation.setIcon(DQLIcon.DYNATRACE_LOGO);
        presentation.setText(DQLBundle.message("editor.action.selectTenant.title"));
        presentation.setText(getSelectedTenant(e.getDataContext()));
        presentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true);
    }

    @Override
    protected @NotNull DefaultActionGroup createPopupActionGroup(@NotNull JComponent button, @NotNull DataContext dataContext) {
        DefaultActionGroup group = new DefaultActionGroup();
        Project project = dataContext.getData(CommonDataKeys.PROJECT);

        if (project == null) {
            return group;
        }

        List<DynatraceTenant> tenants = DynatraceTenantsService.getInstance().getTenants();
        String selectedTenant = getSelectedTenant(dataContext);
        for (DynatraceTenant tenant : tenants) {
            group.add(new AnAction(tenant.getName()) {
                @Override
                public void update(@NotNull AnActionEvent e) {
                    super.update(e);
                    Presentation presentation = e.getPresentation();
                    if (Objects.equals(selectedTenant, tenant.getName())) {
                        presentation.setVisible(false);
                    }
                }

                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    updateSelectedTenant(tenant.getName(), e);
                }

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.BGT;
                }
            });
        }
        group.add(ActionManager.getInstance().getAction("DQL.ManageTenants"));
        return group;
    }

    protected @Nullable String getSelectedTenant(@NotNull DataContext e) {
        QueryConfiguration config = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        return config != null ? config.tenant() : null;
    }

    protected void updateSelectedTenant(@NotNull String selectedTenant, @NotNull AnActionEvent e) {
        QueryConfiguration config = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        if (config != null) {
            config.setTenant(selectedTenant);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public @NotNull JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
        JComponent component = super.createCustomComponent(presentation, place);
        component.setOpaque(false);
        return component;
    }
}

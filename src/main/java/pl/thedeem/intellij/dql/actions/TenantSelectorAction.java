package pl.thedeem.intellij.dql.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;
import pl.thedeem.intellij.dql.services.ui.DQLManagedService;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsConfigurable;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

public class TenantSelectorAction extends ComboBoxAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        Presentation presentation = e.getPresentation();
        if (project == null) {
            presentation.setVisible(false);
            return;
        }
        DQLManagedService<?> service = e.getData(DQLManagedService.EXECUTION_SERVICE);
        if (service instanceof DQLExecutionService executionService && executionService.getConfiguration() != null) {
            presentation.setText(executionService.getConfiguration().tenant());
        } else {
            PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
            if (file == null) {
                presentation.setVisible(false);
                return;
            }
            DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance(project);
            QueryConfiguration configuration = configurationService.getQueryConfiguration(file);
            presentation.setText(configuration.tenant());
        }
        presentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true);
    }

    @Override
    protected @NotNull DefaultActionGroup createPopupActionGroup(@NotNull JComponent button, @NotNull DataContext dataContext) {
        DefaultActionGroup group = new DefaultActionGroup();
        Project project = dataContext.getData(CommonDataKeys.PROJECT);
        PsiFile file = dataContext.getData(CommonDataKeys.PSI_FILE);

        if (project == null) {
            return group;
        }
        DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance(project);
        List<DynatraceTenant> tenants = DynatraceTenantsService.getInstance().getTenants();

        DQLManagedService<?> service = dataContext.getData(DQLManagedService.EXECUTION_SERVICE);
        for (DynatraceTenant tenant : tenants) {
            group.add(new AnAction(tenant.getName()) {
                @Override
                public void update(@NotNull AnActionEvent e) {
                    super.update(e);
                    QueryConfiguration configuration = service instanceof DQLExecutionService executionService && executionService.getConfiguration() != null ?
                            executionService.getConfiguration()
                            : file != null ? configurationService.getQueryConfiguration(file) : null;
                    Presentation presentation = e.getPresentation();
                    if (configuration == null) {
                        presentation.setEnabledAndVisible(false);
                        return;
                    }

                    if (Objects.equals(configuration.tenant(), tenant.getName())) {
                        presentation.setVisible(false);
                    }
                }

                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    if (service instanceof DQLExecutionService executionService) {
                        QueryConfiguration configuration = executionService.getConfiguration();
                        if (configuration != null) {
                            configuration.setTenant(tenant.getName());
                        }
                    } else if (file != null) {
                        QueryConfiguration configuration = configurationService.getQueryConfiguration(file);
                        configuration.setTenant(tenant.getName());
                        configurationService.updateQueryConfiguration(configuration, file);
                    }
                }

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.BGT;
                }
            });
        }
        group.add(new AnAction(DQLBundle.message("action.DQL.SelectTenant.manage", AllIcons.Actions.Edit)) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                ShowSettingsUtil.getInstance().editConfigurable(
                        ProjectManager.getInstance().getDefaultProject(),
                        new DynatraceTenantsConfigurable()
                );
            }
        });
        return group;
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

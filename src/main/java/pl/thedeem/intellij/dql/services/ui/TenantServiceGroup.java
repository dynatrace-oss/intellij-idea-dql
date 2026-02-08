package pl.thedeem.intellij.dql.services.ui;

import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.services.ManagedService;
import pl.thedeem.intellij.common.services.ManagedServiceGroup;
import pl.thedeem.intellij.common.services.ProjectServicesManager;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.actions.OpenDQLQueryConsoleAction;
import pl.thedeem.intellij.dql.fileProviders.DQLQueryConsoleVirtualFile;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.util.List;
import java.util.Objects;

public class TenantServiceGroup implements ManagedServiceGroup {
    private final String tenantId;
    private DefaultActionGroup actions;

    public TenantServiceGroup(@NotNull String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        DynatraceTenant tenant = DynatraceTenantsService.getInstance().findTenant(tenantId);
        return new StandardItemPresentation(tenant != null ? tenant.getName() : tenantId, null, DQLIcon.DYNATRACE_LOGO);
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TenantServiceGroup that = (TenantServiceGroup) o;
        return Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tenantId);
    }

    @Override
    public @Nullable ActionGroup getToolbarActions() {
        if (actions == null) {
            actions = new DefaultActionGroup();
            actions.add(new AnAction(
                    DQLBundle.message("services.tenantGroup.actions.openConsole.title"),
                    null,
                    DQLIcon.QUERY_CONSOLE
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    Project project = e.getProject();
                    if (project == null) {
                        return;
                    }
                    DQLQueryConsoleVirtualFile vf = new DQLQueryConsoleVirtualFile(
                            DQLBundle.message(
                                    "action.DQL.OpenDQLQueryConsole.ServiceViewAction.consoleName",
                                    OpenDQLQueryConsoleAction.COUNTER.incrementAndGet()
                            ),
                            tenantId
                    );
                    FileEditorManager.getInstance(project).openFile(vf, true);
                }
            });
            actions.addAction(new AnAction(
                    DQLBundle.message("services.tenantGroup.actions.closeAll.title"),
                    null,
                    AllIcons.Actions.Close
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    Project project = e.getProject();
                    if (project == null) {
                        return;
                    }
                    ProjectServicesManager manager = ProjectServicesManager.getInstance(project);
                    List<ManagedService> relatedServices = manager.find(s -> s.getParentGroups().contains(TenantServiceGroup.this));
                    for (ManagedService service : relatedServices) {
                        manager.unregisterService(service);
                    }
                }
            });
        }
        return actions;
    }
}

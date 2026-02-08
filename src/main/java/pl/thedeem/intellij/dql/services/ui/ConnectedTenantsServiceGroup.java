package pl.thedeem.intellij.dql.services.ui;

import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.services.ManagedServiceGroup;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.fileProviders.DQLQueryConsoleVirtualFile;
import pl.thedeem.intellij.dql.settings.DQLSettingsConfigurable;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsConfigurable;

import java.util.List;

public class ConnectedTenantsServiceGroup implements ManagedServiceGroup {
    private static final ConnectedTenantsServiceGroup INSTANCE = new ConnectedTenantsServiceGroup();
    private DefaultActionGroup actions;

    protected ConnectedTenantsServiceGroup() {
    }

    public static ConnectedTenantsServiceGroup getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return new StandardItemPresentation(
                DQLBundle.message("services.connectedTenants.groupName"),
                null,
                DQLIcon.DYNATRACE_LOGO
        );
    }

    @Override
    public void dispose() {
    }

    @Override
    public @Nullable ActionGroup getToolbarActions() {
        if (actions == null) {
            actions = new DefaultActionGroup();
            actions.add(new AnAction(
                    DQLBundle.message("services.connectedTenants.actions.openConsole.title"),
                    null,
                    DQLIcon.QUERY_CONSOLE
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    Project project = e.getProject();
                    if (project == null) {
                        return;
                    }
                    DQLQueryConsoleVirtualFile.open(project);
                }
            });
            actions.add(new AnAction(
                    DQLBundle.message("services.connectedTenants.actions.allSettings.title"),
                    null,
                    AllIcons.General.GearPlain
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    Project project = e.getProject();
                    if (project == null) {
                        return;
                    }
                    DQLSettingsConfigurable.showSettings(project);
                }
            });
            actions.addAction(new AnAction(
                    DQLBundle.message("services.connectedTenants.actions.manageTenants.title"),
                    null,
                    DQLIcon.MANAGE_TENANTS
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                    DynatraceTenantsConfigurable.showSettings();
                }

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.BGT;
                }
            });
        }
        return actions;
    }

    @Override
    public @NotNull String getServiceId() {
        return "DynatraceTenants";
    }

    @Override
    public @NotNull List<ManagedServiceGroup> getParentGroups() {
        return List.of();
    }
}

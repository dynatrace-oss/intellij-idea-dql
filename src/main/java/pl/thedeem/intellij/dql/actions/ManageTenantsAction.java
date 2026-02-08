package pl.thedeem.intellij.dql.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsConfigurable;

public class ManageTenantsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        DynatraceTenantsConfigurable.showSettings();
    }
}

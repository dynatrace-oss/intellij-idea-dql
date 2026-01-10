package pl.thedeem.intellij.dql.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsConfigurable;

public class ManageTenantsAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        ShowSettingsUtil.getInstance().editConfigurable(
                ProjectManager.getInstance().getDefaultProject(),
                new DynatraceTenantsConfigurable()
        );
    }
}

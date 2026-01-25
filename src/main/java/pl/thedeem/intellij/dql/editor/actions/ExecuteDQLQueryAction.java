package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.actions.ActionUtils;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;
import pl.thedeem.intellij.dql.exec.DQLProcessHandler;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.ui.DQLServicesManager;

public abstract class ExecuteDQLQueryAction extends AnAction {
    public ExecuteDQLQueryAction() {
        super();
        Presentation presentation = getTemplatePresentation();
        presentation.setIcon(AllIcons.Actions.Execute);
        presentation.setText(DQLBundle.message("gutter.executeDQL.action.execute"));
    }

    @Override
    public void update(@NotNull AnActionEvent original) {
        AnActionEvent e = updateEvent(original);
        QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        if (configuration == null || configuration.tenant() == null) {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent original) {
        AnActionEvent e = updateEvent(original);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        if (file == null || configuration == null) {
            return;
        }
        Project project = file.getProject();
        String serviceName = ActionUtils.generateServiceName(file);
        DQLServicesManager.getInstance(project)
                .startExecution(new DQLExecutionService(serviceName, project, new DQLProcessHandler()), configuration);

    }

    protected @NotNull AnActionEvent updateEvent(@NotNull AnActionEvent e) {
        return e;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

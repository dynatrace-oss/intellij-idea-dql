package pl.thedeem.intellij.dql.actions;

import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.services.ProjectServicesManager;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;
import pl.thedeem.intellij.dql.exec.DQLProcessHandler;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dql.services.notifications.DQLNotificationsService;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import java.util.List;
import java.util.Objects;

public class ExecuteDQLQueryAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent original) {
        AnActionEvent e = updateEvent(original);
        if (isNotRelatedToDQL(original)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        QueryConfiguration configuration = Objects.requireNonNullElse(
                e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION),
                file != null ? DQLQueryConfigurationService.getInstance().getQueryConfiguration(file) : new QueryConfiguration()
        );
        if (configuration.tenant() == null) {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent original) {
        AnActionEvent e = updateEvent(original);
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        QueryConfiguration configuration = Objects.requireNonNullElse(
                e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION),
                file != null ? DQLQueryConfigurationService.getInstance().getQueryConfiguration(file) : new QueryConfiguration()
        );
        if (file == null || configuration.tenant() == null) {
            if (e.getProject() != null) {
                DQLNotificationsService.getInstance(e.getProject()).showErrorNotification(
                        DQLBundle.message("action.DQL.ExecuteDQLQuery.notifications.missingTenant.title"),
                        DQLBundle.message("action.DQL.ExecuteDQLQuery.notifications.missingTenant.description"),
                        e.getProject(),
                        List.of()
                );
            }
            return;
        }
        Project project = file.getProject();
        DQLExecutionService service = new DQLExecutionService(
                DQLBundle.message("services.executeDQL.serviceName", file.getName()),
                configuration,
                project,
                new DQLProcessHandler()
        );
        ProjectServicesManager.getInstance(project).registerService(service);
        service.startExecution();
    }

    protected @NotNull AnActionEvent updateEvent(@NotNull AnActionEvent e) {
        return e;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    protected boolean isNotRelatedToDQL(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null) {
            return true;
        }
        if (DQLFileType.INSTANCE.equals(file.getFileType())) {
            return false;
        }
        if (PsiTreeUtil.getChildrenOfType(file, DQLQuery.class) == null) {
            return true;
        }
        Language[] langs = e.getData(LangDataKeys.CONTEXT_LANGUAGES);
        return langs == null || !List.of(langs).contains(DynatraceQueryLanguage.INSTANCE);
    }
}

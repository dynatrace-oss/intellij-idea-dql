package pl.thedeem.intellij.dql.actions.executionToolbar;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.actions.ActionUtils;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;
import pl.thedeem.intellij.dql.executing.DQLProcessHandler;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.ui.DQLManagedService;
import pl.thedeem.intellij.dql.services.ui.DQLServicesManager;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StartStopDQLExecutionAction extends AnAction {
    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Presentation presentation = e.getPresentation();
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        if (project == null || (service == null && ActionUtils.isNotRelatedToDQL(e))) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        List<DQLManagedService<?>> services = runningServices(project, file, service);
        if (!services.isEmpty()) {
            presentation.setText(DQLBundle.message("action.DQL.StopQuery.text"));
            presentation.setIcon(AllIcons.Run.Stop);
        } else {
            presentation.setText(DQLBundle.message("action.DQL.StartStopExecution.text"));
            presentation.setIcon(AllIcons.Actions.Execute);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        if (project == null) {
            return;
        }
        Editor editor = ActionUtils.getEditor(e);
        PsiFile file = ActionUtils.getRelatedPsiFile(e.getData(CommonDataKeys.PSI_FILE), editor, e.getData(CommonDataKeys.PSI_ELEMENT));

        if (service != null) {
            List<DQLManagedService<?>> services = runningServices(project, file, service);
            if (!services.isEmpty()) {
                for (DQLManagedService<?> related : services) {
                    related.stopExecution();
                }
                ActivityTracker.getInstance().inc();
                return;
            }
            DQLServicesManager.getInstance(project).startExecution(new DQLExecutionService(service.getName(), project, new DQLProcessHandler()), service.getConfiguration());
            return;
        }

        if (file == null) {
            return;
        }

        withQueryConfiguration(project, editor, file, e, (QueryConfiguration configuration) -> {
            e.getPresentation().setEnabled(false);
            String serviceName = ActionUtils.generateServiceName(file);
            DQLServicesManager.getInstance(project).startExecution(new DQLExecutionService(serviceName, project, new DQLProcessHandler()), configuration);
            ActivityTracker.getInstance().inc();
        });
    }

    protected void withQueryConfiguration(
            @NotNull Project project,
            @Nullable Editor editor,
            @NotNull PsiFile file,
            @NotNull AnActionEvent e,
            @NotNull Consumer<QueryConfiguration> consumer
    ) {
        QueryConfiguration config = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        DQLExecutionService service = ActionUtils.getService(e, DQLExecutionService.class);
        if (config == null) {
            return;
        }
        if (service != null) {
            e.getPresentation().setEnabled(false);
            DQLServicesManager.getInstance(project).startExecution(
                    new DQLExecutionService(service.getName(), project, new DQLProcessHandler()),
                    service.getConfiguration()
            );
            return;
        }

        if (editor == null) {
            return;
        }

        DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance(project);

        configurationService.getQueryFromEditorContext(file, editor, (query) -> {
            config.setQuery(query);
            consumer.accept(config);
        });
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private List<DQLManagedService<?>> runningServices(@NotNull Project project, @Nullable PsiFile file, @Nullable DQLExecutionService service) {
        String serviceName = service != null ? service.getName() : file != null ? ActionUtils.generateServiceName(file) : null;
        return DQLServicesManager.getInstance(project).findServices(ex -> ex instanceof DQLExecutionService && ex.isRunning() && Objects.equals(ex.getName(), serviceName));
    }
}

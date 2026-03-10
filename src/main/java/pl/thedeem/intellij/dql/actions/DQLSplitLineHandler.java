package pl.thedeem.intellij.dql.actions;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.services.ProjectServicesManager;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;
import pl.thedeem.intellij.dql.exec.DQLProcessHandler;
import pl.thedeem.intellij.dql.services.notifications.DQLNotificationsService;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import java.util.List;
import java.util.Objects;

import static pl.thedeem.intellij.dql.actions.ExecuteDQLQueryAction.PREFERRED_EXECUTION_NAME;

public class DQLSplitLineHandler extends EditorActionHandler {
    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, @NotNull DataContext e) {
        Project project = editor.getProject();
        PsiFile file = project != null ? PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument()) : null;
        if (project == null || file == null || !DQLFileType.INSTANCE.equals(file.getFileType())) {
            EditorActionManager.getInstance()
                    .getActionHandler(IdeActions.ACTION_EDITOR_SPLIT)
                    .execute(editor, caret, e);
            return;
        }

        QueryConfiguration configuration = Objects.requireNonNullElse(
                e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION),
                DQLQueryConfigurationService.getInstance().getQueryConfiguration(file)
        );

        if (configuration.tenant() == null) {
            DQLNotificationsService.getInstance(project).showErrorNotification(
                    DQLBundle.message("action.DQL.ExecuteDQLQuery.notifications.missingTenant.title"),
                    DQLBundle.message("action.DQL.ExecuteDQLQuery.notifications.missingTenant.description"),
                    project,
                    List.of()
            );
            return;
        }
        DQLQueryConfigurationService.getInstance().getQueryFromEditorContext(file, editor, (consumer) -> {
            QueryConfiguration config = configuration.copy();
            config.setQuery(consumer);
            DQLExecutionService service = new DQLExecutionService(
                    DQLBundle.message(
                            "services.executeDQL.serviceName",
                            Objects.requireNonNullElseGet(e.getData(PREFERRED_EXECUTION_NAME), file::getName)
                    ),
                    config,
                    project,
                    new DQLProcessHandler()
            );
            ProjectServicesManager.getInstance(project).registerService(service);
            service.startExecution();
        });
    }
}

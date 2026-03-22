package pl.thedeem.intellij.dql.editor;

import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.services.ProjectServicesManager;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;
import pl.thedeem.intellij.dql.exec.DQLProcessHandler;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.query.DQLQuerySelectorService;
import pl.thedeem.intellij.dql.services.query.model.QueryConfiguration;

import java.util.Objects;

import static pl.thedeem.intellij.dql.actions.ExecuteDQLQueryAction.PREFERRED_EXECUTION_NAME;

public class DQLSplitLineHandler extends EditorActionHandler {
    private final EditorActionHandler originalHandler;

    public DQLSplitLineHandler(EditorActionHandler originalHandler) {
        this.originalHandler = originalHandler;
    }

    @Override
    protected void doExecute(@NotNull Editor editor, @Nullable Caret caret, @NotNull DataContext e) {
        Project project = editor.getProject();
        PsiFile hostFile = project != null ? PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument()) : null;
        if (project == null || hostFile == null) {
            originalHandler.execute(editor, caret, e);
            return;
        }

        PsiFile dqlFile = resolveDqlFile(hostFile, editor, project);
        if (dqlFile == null) {
            originalHandler.execute(editor, caret, e);
            return;
        }

        QueryConfiguration configuration = Objects.requireNonNullElse(
                e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION),
                DQLQueryConfigurationService.getInstance().getQueryConfiguration(dqlFile)
        );

        if (configuration.tenant() == null) {
            originalHandler.execute(editor, caret, e);
            return;
        }
        DQLQuerySelectorService.getInstance().getQueryFromEditorContext(dqlFile, editor, (query) -> {
            QueryConfiguration config = configuration.copy();
            config.setQuery(query);
            DQLExecutionService service = new DQLExecutionService(
                    DQLBundle.message(
                            "services.executeDQL.serviceName",
                            Objects.requireNonNullElseGet(e.getData(PREFERRED_EXECUTION_NAME), dqlFile::getName)
                    ),
                    config,
                    project,
                    new DQLProcessHandler()
            );
            ProjectServicesManager.getInstance(project).registerService(service);
            service.startExecution();
        });
    }

    private @Nullable PsiFile resolveDqlFile(@NotNull PsiFile hostFile, @NotNull Editor editor, @NotNull Project project) {
        if (DQLFileType.INSTANCE.equals(hostFile.getFileType())) {
            return hostFile;
        }

        FileViewProvider viewProvider = hostFile.getViewProvider();
        PsiFile dqlRoot = viewProvider.getPsi(DynatraceQueryLanguage.INSTANCE);
        if (dqlRoot != null) {
            return dqlRoot;
        }

        InjectedLanguageManager injectedManager = InjectedLanguageManager.getInstance(project);
        PsiElement injectedElement = injectedManager.findInjectedElementAt(hostFile, editor.getCaretModel().getOffset());
        if (injectedElement != null) {
            PsiFile injectedFile = injectedElement.getContainingFile();
            if (DQLFileType.INSTANCE.equals(injectedFile.getFileType())) {
                return injectedFile;
            }
        }
        return null;
    }
}

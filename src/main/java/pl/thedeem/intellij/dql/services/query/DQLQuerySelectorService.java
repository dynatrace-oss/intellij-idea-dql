package pl.thedeem.intellij.dql.services.query;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.DQLQuery;

import java.util.function.Consumer;

public interface DQLQuerySelectorService {
    static @NotNull DQLQuerySelectorService getInstance() {
        return ApplicationManager.getApplication().getService(DQLQuerySelectorService.class);
    }

    @NotNull String getQueryText(@NotNull PsiFile file);

    @NotNull String getQueryText(@NotNull DQLQuery query, @NotNull Project project);

    void getQueryFromEditorContext(@NotNull PsiFile file, @Nullable Editor editor, @NotNull Consumer<@NotNull String> consumer);
}

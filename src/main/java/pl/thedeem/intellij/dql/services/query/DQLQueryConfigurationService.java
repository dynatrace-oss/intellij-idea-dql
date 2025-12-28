package pl.thedeem.intellij.dql.services.query;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;

import java.util.function.Consumer;

public interface DQLQueryConfigurationService {
    Key<QueryConfiguration> QUERY_CONFIGURATION = Key.create("DQL_QUERY_CONFIGURATION");

    static @NotNull DQLQueryConfigurationService getInstance(@NotNull Project project) {
        return project.getService(DQLQueryConfigurationService.class);
    }

    @NotNull QueryConfiguration getQueryConfiguration(@NotNull PsiFile file);

    void getQueryConfigurationWithEditorContext(@NotNull PsiFile file, @Nullable Editor editor, @NotNull Consumer<QueryConfiguration> consumer);

    @NotNull QueryConfiguration createDefaultConfiguration(@NotNull PsiFile file);

    void updateQueryConfiguration(@NotNull QueryConfiguration configuration, @NotNull PsiFile file);
}

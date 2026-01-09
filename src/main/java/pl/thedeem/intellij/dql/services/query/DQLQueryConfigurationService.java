package pl.thedeem.intellij.dql.services.query;

import com.intellij.openapi.actionSystem.DataKey;
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
    DataKey<QueryConfiguration> DATA_QUERY_CONFIGURATION = DataKey.create("DQL_QUERY_CONFIGURATION");

    static @NotNull DQLQueryConfigurationService getInstance(@NotNull Project project) {
        return project.getService(DQLQueryConfigurationService.class);
    }

    @NotNull QueryConfiguration getQueryConfiguration(@NotNull PsiFile file);

    @NotNull QueryConfiguration createDefaultConfiguration(@NotNull PsiFile file);

    void getQueryFromEditorContext(@NotNull PsiFile file, @Nullable Editor editor, @NotNull Consumer<@NotNull String> consumer);
}

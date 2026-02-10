package pl.thedeem.intellij.dql.services.variables;

import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface DQLVariablesService {
    static DQLVariablesService getInstance(@NotNull Project project) {
        return project.getService(DQLVariablesService.class);
    }

    @Nullable Path getDefaultVariablesFile(@NotNull PsiElement element);

    @NotNull List<PsiElement> findVariableDefinitionFiles(@NotNull String variableName, @NotNull PsiFile file);

    @NotNull PsiElement findClosestDefinition(@NotNull String path, @NotNull List<PsiElement> definitions);

    @Nullable String getVariableValue(@Nullable JsonValue value);

    @NotNull List<VariableDefinition> getDefinedVariables(@NotNull PsiFile file);

    record VariableDefinition(String name, String value, Collection<String> dataTypes) {
    }
}

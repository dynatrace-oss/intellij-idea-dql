package pl.thedeem.intellij.dql.services.variables;

import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonValue;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;

public interface DQLVariablesService {
    String DQL_VARIABLES_FILE = "dql-variables.json";

    static DQLVariablesService getInstance(@NotNull Project project) {
        return project.getService(DQLVariablesService.class);
    }

    @Nullable Path getDefaultVariablesFile(@NotNull PsiElement element);

    @NotNull List<PsiElement> findVariableDefinitionFiles(@NotNull String variableName, @NotNull PsiFile file);

    @RequiresReadLock
    @NotNull List<DQLVariableExpression> findVariableUsages(@NotNull JsonProperty definition);

    @NotNull PsiElement findClosestDefinition(@NotNull String path, @NotNull List<PsiElement> definitions);

    @Nullable String getVariableValue(@Nullable JsonValue value);

    @RequiresReadLock
    @NotNull List<VariableDefinition> getDefinedVariables(@NotNull PsiFile file);

    record VariableDefinition(String name, String value, Collection<String> dataTypes) {
    }
}

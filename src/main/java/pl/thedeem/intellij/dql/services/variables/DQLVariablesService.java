package pl.thedeem.intellij.dql.services.variables;

import com.intellij.json.psi.JsonProperty;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DQLVariablesService {
    String DQL_VARIABLES_FILE = "dql-variables.json";
    Key<Map<String, VariableDefinition>> RUNTIME_VALUES_KEY = Key.create("DQL_RUNTIME_VARIABLE_VALUES");

    static DQLVariablesService getInstance(@NotNull Project project) {
        return project.getService(DQLVariablesService.class);
    }

    @Nullable Path getDefaultVariablesFile(@NotNull PsiElement element);

    @NotNull List<PsiElement> findVariableDefinitionFiles(@NotNull String variableName, @NotNull PsiFile file);

    @RequiresReadLock
    @NotNull List<DQLVariableExpression> findVariableUsages(@NotNull JsonProperty definition);

    @Nullable VariableDefinition loadVariable(@NotNull PsiFile file, @NotNull String variableName);

    @RequiresReadLock
    @NotNull List<VariableDefinition> getDefinedVariables(@NotNull PsiFile file);

    @RequiresReadLock
    @NotNull Collection<VariableDefinition> getUserDefinedVariables(@NotNull PsiFile file);

    @RequiresReadLock
    @NotNull Set<String> getUndefinedVariables(@NotNull PsiFile file);

    void updateUserDefinedVariables(@NotNull PsiFile file, @NotNull Collection<VariableDefinition> definitions);

    @NotNull ModificationTracker getUserDefinedVariablesTracker(@NotNull PsiFile file);

    record VariableDefinition(String name, String value, Collection<String> dataTypes) {
    }
}

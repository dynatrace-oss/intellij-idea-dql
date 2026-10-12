package pl.thedeem.intellij.dql.services.definition;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.services.definition.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface DQLDefinitionService {
    static @NotNull DQLDefinitionService getInstance(@NotNull Project project) {
        return project.getService(DQLDefinitionService.class);
    }

    void invalidateCache();

    @Nullable DataType findDataType(@NotNull String name);

    @Nullable ParameterValueType findParameterValueType(@NotNull String name);

    @NotNull List<Function> getFunctionByName(@NotNull String name);

    @NotNull Collection<Function> getFunctionsByCategory(@NotNull Predicate<String> filter);

    @NotNull Collection<Function> getFunctionsByCategoryAndReturnType(@NotNull Predicate<String> category, @NotNull Predicate<String> values);

    @Nullable Collection<String> getFunctionCategoriesForParameterTypes(@NotNull Collection<String> parameterValueTypes);

    @NotNull Collection<Function> getFunctions();

    @Nullable Command getCommandByName(@NotNull String name);

    @NotNull Collection<String> getDataSourceCommands();

    @NotNull Collection<Command> getCommands();

    @Nullable Operator getOperator(@NotNull String operatorId);

    @NotNull Collection<String> getStringDataTypes();

    @NotNull Collection<String> getNumericDataTypes();

    @NotNull Collection<String> getStringParameterValueTypes();

    @NotNull Collection<String> getDplParameterValueTypes();

    @NotNull Collection<String> getExecutionBlockParameterValueTypes();

    @NotNull Collection<String> getFieldIdentifierParameterValueTypes();

    @NotNull Map<String, String> getFilterCommandNegations();
}

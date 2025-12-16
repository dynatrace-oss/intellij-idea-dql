package pl.thedeem.intellij.dql.definition;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public interface DQLDefinitionService {
    Predicate<String> STARTING_COMMANDS = "data_source"::equals;
    Predicate<String> EXTENSION_COMMANDS = STARTING_COMMANDS.negate();
    Predicate<String> NUMERICAL_TYPES = s -> Set.of("dql.dataType.double", "dql.dataType.long").contains(s);
    Predicate<String> BOOLEAN_TYPES = s -> Objects.equals("dql.dataType.boolean", s);
    Predicate<String> TIME_TYPES = s -> Set.of("ql.dataType.timestamp", "dql.dataType.duration", "dql.dataType.timeframe").contains(s);
    Predicate<String> COMPARABLE_TYPES = s -> NUMERICAL_TYPES.test(s) || TIME_TYPES.test(s) || Objects.equals("dql.dataType.string", s);
    Set<String> STRING_PARAMETER_VALUE_TYPES = Set.of(
            "dql.parameterValueType.bucket",
            "dql.parameterValueType.dplPattern",
            "dql.parameterValueType.filePattern",
            "dql.parameterValueType.jsonPath",
            "dql.parameterValueType.namelessDplPattern",
            "dql.parameterValueType.prefix",
            "dql.parameterValueType.tabularFileExisting",
            "dql.parameterValueType.tabularFileNew",
            "dql.parameterValueType.url"
    );
    Set<String> STRING_VALUE_TYPES = Set.of("dql.dataType.uid", "dql.dataType.string", "dql.dataType.ip");
    Set<String> NUMERIC_VALUE_TYPES = Set.of("dql.dataType.double", "dql.dataType.long");
    Set<String> DPL_VALUE_TYPES = Set.of("dql.parameterValueType.namelessDplPattern", "dql.parameterValueType.dplPattern");
    Set<String> EXECUTION_PARAMETER_VALUE_TYPES = Set.of("dql.parameterValueType.nonEmptyExecutionBlock", "dql.parameterValueType.executionBlock");
    Set<String> FIELD_IDENTIFIER_PARAMETER_VALUE_TYPES = Set.of(
            "dql.parameterValueType.fieldPattern",
            "dql.parameterValueType.identifierForFieldOnRootLevel",
            "dql.parameterValueType.identifierForAnyField",
            "dql.parameterValueType.dataObject"
    );

    static @NotNull DQLDefinitionService getInstance(@NotNull Project project) {
        return project.getService(DQLDefinitionService.class);
    }

    void invalidateCache();

    @Nullable DataType findDataType(@NotNull String name);

    @Nullable ParameterValueType findParameterValueType(@NotNull String name);

    @NotNull List<Function> getFunctionByName(@NotNull String name);

    @NotNull Collection<Function> getFunctionsByReturnType(@NotNull Predicate<String> filter);

    @NotNull Collection<Function> getFunctionsByCategory(@NotNull Predicate<String> filter);

    @NotNull Collection<Function> getFunctionsByCategoryAndReturnType(@NotNull Predicate<String> category, @NotNull Predicate<String> values);

    @Nullable Collection<String> getFunctionCategoriesForParameterTypes(@NotNull Collection<String> parameterValueTypes);

    @NotNull Collection<Function> getFunctions();

    @Nullable Command getCommandByName(@NotNull String name);

    @NotNull Collection<Command> getCommandsByCategory(@NotNull Predicate<String> filter);

    @NotNull Collection<Command> getCommands();

    @Nullable Operator getOperator(@NotNull String operatorId);
}

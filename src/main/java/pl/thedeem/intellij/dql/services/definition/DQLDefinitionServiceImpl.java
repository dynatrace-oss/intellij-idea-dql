package pl.thedeem.intellij.dql.services.definition;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.DefinitionUtils;
import pl.thedeem.intellij.dql.definition.model.*;

import java.util.*;
import java.util.function.Predicate;

public class DQLDefinitionServiceImpl implements DQLDefinitionService {
    private static final String DEFINITION_FILE = "definition/dql.json";
    private static final String DEFINITION_OVERRIDES_FILE = "definition/dql.overrides.json";

    private final Project project;
    private final SimpleModificationTracker tracker;
    private CachedValue<DQLDefinition> definition;
    private CachedValue<Map<String, String>> commandsMapping;
    private CachedValue<Map<String, Set<String>>> functionsMapping;
    private CachedValue<Map<String, List<Command>>> commandsByCategory;
    private CachedValue<Map<String, List<Function>>> functionsByDataType;
    private CachedValue<Map<String, List<Function>>> functionsByCategory;

    public DQLDefinitionServiceImpl(@NotNull Project project) {
        this.project = project;
        this.tracker = new SimpleModificationTracker();
    }

    @Override
    public void invalidateCache() {
        tracker.incModificationCount();
    }

    @Override
    public @Nullable DataType findDataType(@NotNull String name) {
        return getDefinition().dataTypes().get(name);
    }

    @Override
    public @Nullable ParameterValueType findParameterValueType(@NotNull String name) {
        return getDefinition().parameterValueTypes().get(name);
    }

    @Override
    public @NotNull List<Function> getFunctionByName(@NotNull String name) {
        if (this.functionsMapping == null) {
            this.functionsMapping = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loadFunctionsMapping(), tracker),
                    false);
        }
        Set<String> id = this.functionsMapping.getValue().get(name.toLowerCase());
        return id != null ? id.stream().map(s -> getDefinition().functions().get(s)).toList() : List.of();
    }

    @Override
    public @NotNull Collection<Function> getFunctionsByReturnType(@NotNull Predicate<String> filter) {
        if (this.functionsByDataType == null) {
            this.functionsByDataType = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loadFunctionsByDataType(), tracker),
                    false);
        }
        return functionsByDataType.getValue().entrySet().stream()
                .filter(e -> filter.test(e.getKey()))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public @NotNull Collection<Function> getFunctionsByCategory(@NotNull Predicate<String> filter) {
        if (this.functionsByCategory == null) {
            this.functionsByCategory = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loadFunctionsByCategories(), tracker),
                    false);
        }
        return functionsByCategory.getValue().entrySet().stream()
                .filter(e -> filter.test(e.getKey()))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public @NotNull Collection<Function> getFunctionsByCategoryAndReturnType(@NotNull Predicate<String> category, @NotNull Predicate<String> values) {
        return getFunctionsByCategory(category).stream()
                .filter(e -> e.signatures().getFirst().outputs().stream().anyMatch(values))
                .toList();
    }

    @Override
    public @Nullable Collection<String> getFunctionCategoriesForParameterTypes(@NotNull Collection<String> parameterValueTypes) {
        // if the parameter requires fields names, do not suggest functions
        if (parameterValueTypes.stream().anyMatch(FIELD_IDENTIFIER_PARAMETER_VALUE_TYPES::contains)) {
            return null;
        }
        // if the parameter requires a primitive value, do not suggest functions
        if (parameterValueTypes.contains("dql.parameterValueType.primitiveValue") || parameterValueTypes.stream().anyMatch(STRING_PARAMETER_VALUE_TYPES::contains)) {
            return null;
        }
        Set<String> categories = new HashSet<>();
        for (String parameterValueType : parameterValueTypes) {
            ParameterValueType type = findParameterValueType(parameterValueType);
            if (type != null && type.allowedFunctionCategories() != null) {
                categories.addAll(type.allowedFunctionCategories());
            }
        }
        return categories;
    }

    @Override
    public @NotNull Collection<Function> getFunctions() {
        return getDefinition().functions().values();
    }

    @Override
    public @Nullable Command getCommandByName(@NotNull String name) {
        if (this.commandsMapping == null) {
            this.commandsMapping = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loadCommandMapping(), tracker),
                    false);
        }
        String id = this.commandsMapping.getValue().get(name.toLowerCase());
        if (id == null) {
            return null;
        }
        return getDefinition().commands().get(id);
    }

    @Override
    public @NotNull Collection<Command> getCommandsByCategory(@NotNull Predicate<String> filter) {
        if (this.commandsByCategory == null) {
            this.commandsByCategory = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loadCommandsByCategories(), tracker),
                    false);
        }

        return commandsByCategory.getValue().entrySet().stream()
                .filter(e -> filter.test(e.getKey()))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public @NotNull Collection<Command> getCommands() {
        return getDefinition().commands().values();
    }

    @Override
    public @Nullable Operator getOperator(@NotNull String operatorId) {
        return getDefinition().operators().get(operatorId);
    }

    private @NotNull DQLDefinition getDefinition() {
        if (this.definition == null) {
            definition = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loadDefinition(), tracker),
                    false
            );
        }
        return this.definition.getValue();
    }

    private @NotNull DQLDefinition loadDefinition() {
        DQLDefinition dqlDefinition = DefinitionUtils.loadDefinitionFromFile(DEFINITION_FILE, DQLDefinition.class);
        if (dqlDefinition != null) {
            return DefinitionUtils.mergeDefinitions(dqlDefinition, DEFINITION_OVERRIDES_FILE);
        }
        return DQLDefinition.empty();
    }

    private @NotNull Map<String, List<Command>> loadCommandsByCategories() {
        Map<String, List<Command>> result = new HashMap<>();
        Map<String, Command> commands = getDefinition().commands();
        for (Command value : commands.values()) {
            result.putIfAbsent(value.category(), new ArrayList<>());
            result.get(value.category()).add(value);
        }
        return result;
    }

    private @NotNull Map<String, List<Function>> loadFunctionsByCategories() {
        Map<String, List<Function>> result = new HashMap<>();
        Map<String, Function> functions = getDefinition().functions();
        for (Function value : functions.values()) {
            result.putIfAbsent(value.category(), new ArrayList<>());
            result.get(value.category()).add(value);
        }
        return result;
    }

    private @NotNull Map<String, String> loadCommandMapping() {
        DQLDefinition dqlDefinition = getDefinition();
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, Command> commands : dqlDefinition.commands().entrySet()) {
            result.put(commands.getValue().name().toLowerCase(), commands.getKey());
        }
        return result;
    }

    private @NotNull Map<String, Set<String>> loadFunctionsMapping() {
        DQLDefinition dqlDefinition = getDefinition();
        Map<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Function> functions : dqlDefinition.functions().entrySet()) {
            String name = functions.getValue().name().toLowerCase();
            result.putIfAbsent(name, new HashSet<>());
            result.get(name).add(functions.getKey());
        }
        return result;
    }

    private @NotNull Map<String, List<Function>> loadFunctionsByDataType() {
        Map<String, List<Function>> result = new HashMap<>();
        for (Function function : getFunctions()) {
            for (Signature signature : function.signatures()) {
                for (String output : signature.outputs()) {
                    result.putIfAbsent(output, new ArrayList<>());
                    List<Function> included = result.get(output);
                    if (!included.contains(function)) {
                        included.add(function);
                    }
                }
            }
        }
        return result;
    }
}

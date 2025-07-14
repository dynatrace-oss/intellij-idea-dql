package pl.thedeem.intellij.dql.definition;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.*;

@Service(Service.Level.PROJECT)
public final class DQLDefinitionService implements Disposable {
    private static final Logger logger = Logger.getInstance(DQLDefinitionService.class);

    private final Project project;
    private final DQLDefinitionLoader loader;
    private CachedValue<Map<String, DQLCommandDefinition>> dqlCommands;
    private CachedValue<Map<DQLCommandGroup, Set<DQLCommandDefinition>>> commandsByType;
    private CachedValue<Map<String, DQLFunctionDefinition>> dqlFunctions;
    private CachedValue<List<DQLParameterDefinition>> timeSeriesParameters;
    private CachedValue<Map<String, Set<DQLFunctionDefinition>>> functionsByType;
    private CachedValue<Map<DQLFunctionGroup, Set<String>>> functionsByGroup;
    private CachedValue<Map<IElementType, DQLOperationTarget>> dqlOperations;

    public DQLDefinitionService(@NotNull Project project) {
        this.project = project;
        this.loader = new DQLLocalDefinitionLoader();
    }

    public DQLDefinitionService(@NotNull Project project,  @NotNull DQLDefinitionLoader loader) {
        this.project = project;
        this.loader = loader;
    }

    public static @NotNull DQLDefinitionService getInstance(@NotNull Project project) {
        return project.getService(DQLDefinitionService.class);
    }

    public @NotNull Map<String, DQLCommandDefinition> getCommands() {
        if (dqlCommands == null) {
            dqlCommands = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loader.loadCommands(), ModificationTracker.NEVER_CHANGED),
                    false
            );
        }
        return dqlCommands.getValue();
    }

    public @NotNull Map<String, DQLFunctionDefinition> getFunctions() {
        if (dqlFunctions == null) {
            dqlFunctions = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loader.loadFunctions(), ModificationTracker.NEVER_CHANGED),
                    false
            );
        }
        return dqlFunctions.getValue();
    }

    public @NotNull List<DQLParameterDefinition> getTimeSeriesParameters() {
        if (timeSeriesParameters == null) {
            timeSeriesParameters = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loader.loadTimeSeriesParameters(), ModificationTracker.NEVER_CHANGED),
                    false
            );
        }
        return timeSeriesParameters.getValue();
    }

    public @NotNull Map<IElementType, DQLOperationTarget> getOperations() {
        if (dqlOperations == null) {
            dqlOperations = CachedValuesManager.getManager(project).createCachedValue(
                () -> new CachedValueProvider.Result<>(loader.loadOperations(), ModificationTracker.NEVER_CHANGED),
                false
            );
        }
        return dqlOperations.getValue();
    }

    public @NotNull Map<DQLCommandGroup, Set<DQLCommandDefinition>> getCommandsByType() {
        if (commandsByType == null) {
            commandsByType = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(reloadCommandsByType(), ModificationTracker.NEVER_CHANGED),
                    false
            );
        }
        return commandsByType.getValue();
    }

    public @NotNull Map<String, Set<DQLFunctionDefinition>> getFunctionsByType() {
        if (functionsByType == null) {
            functionsByType = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(reloadFunctionsByType(), ModificationTracker.NEVER_CHANGED),
                    false
            );
        }
        return functionsByType.getValue();
    }

    public @NotNull Map<DQLFunctionGroup, Set<String>> getFunctionsByGroup() {
        if (functionsByGroup == null) {
            functionsByGroup = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(reloadFunctionsByGroup(), ModificationTracker.NEVER_CHANGED),
                    false
            );
        }
        return functionsByGroup.getValue();
    }

    public @Nullable DQLCommandDefinition getCommand(@NotNull DQLQueryStatement command) {
        return getCommand(command.getName());
    }

    public @Nullable DQLCommandDefinition getCommand(@Nullable String commandName) {
        return commandName != null ? getCommands().get(commandName.toLowerCase()) : null;
    }

    public @Nullable DQLFunctionDefinition getFunction(@Nullable String functionName) {
        return functionName != null ? getFunctions().get(functionName.toLowerCase()) : null;
    }

    public @NotNull Set<DQLDataType> getResultType(@Nullable ExpressionOperatorImpl operator, @Nullable DQLExpression left, @Nullable DQLExpression right) {
        if (operator != null && left instanceof BaseTypedElement leftEl && right instanceof BaseTypedElement rightEl) {
            return getResultType(operator.getNodeType(), leftEl.getDataType(), rightEl.getDataType());
        }
        return Set.of(DQLDataType.UNKNOWN);
    }

    public @Nullable DQLOperationTarget getTargetType(@Nullable ExpressionOperatorImpl operator) {
        if (operator != null) {
            Map<IElementType, DQLOperationTarget> operations = getOperations();
            return operations.get(operator.getNodeType());
        }
        return null;
    }

    public @NotNull Set<DQLDataType> getResultType(@NotNull IElementType operator, @NotNull Set<DQLDataType> left, @NotNull Set<DQLDataType> right) {
        Map<IElementType, DQLOperationTarget> operations = getOperations();
        DQLOperationTarget target = operations.get(operator);
        if (target == null) {
            return Set.of(DQLDataType.UNKNOWN);
        }
        return target.getMapping(left, right);
    }

    public @NotNull List<DQLCommandDefinition> getStartingCommands() {
        List<DQLCommandDefinition> result = new ArrayList<>();
        Map<DQLCommandGroup, Set<DQLCommandDefinition>> byType = getCommandsByType();
        for (DQLCommandGroup startingCommandType : DQLCommandGroup.STARTING_COMMAND_TYPES) {
            result.addAll(byType.getOrDefault(startingCommandType, Set.of()));
        }
        return Collections.unmodifiableList(result);
    }

    public @NotNull List<DQLCommandDefinition> getExtensionCommands() {
        List<DQLCommandDefinition> result = new ArrayList<>();
        Map<DQLCommandGroup, Set<DQLCommandDefinition>> byType = getCommandsByType();
        for (DQLCommandGroup startingCommandType : DQLCommandGroup.EXTENSION_COMMAND_TYPES) {
            result.addAll(byType.getOrDefault(startingCommandType, Set.of()));
        }
        return Collections.unmodifiableList(result);
    }

    public @NotNull Set<DQLFunctionDefinition> getFunctionByNames(@NotNull Set<String> functionName) {
        Map<String, DQLFunctionDefinition> functions = getFunctions();
        Set<DQLFunctionDefinition> result = new HashSet<>();
        for (String name : functionName) {
            DQLFunctionDefinition definition = functions.get(name.toLowerCase());
            if (definition != null) {
                result.add(definition);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    public @NotNull Set<DQLFunctionDefinition> getFunctionByTypes(@NotNull Set<DQLDataType> types) {
        Map<String, Set<DQLFunctionDefinition>> functionsByType = getFunctionsByType();
        Set<DQLFunctionDefinition> result = new HashSet<>();
        for (String type : DQLDataType.getAllTypes(types)) {
            Set<DQLFunctionDefinition> functionsForType = functionsByType.get(type);
            if (functionsForType != null) {
                result.addAll(functionsForType);
            }
        }

        return Collections.unmodifiableSet(result);
    }

    public @NotNull Set<String> getFunctionNamesByGroups(@NotNull Set<DQLFunctionGroup> groups) {
        Map<DQLFunctionGroup, Set<String>> functionsByGroup = getFunctionsByGroup();
        Set<String> result = new HashSet<>();
        for (DQLFunctionGroup group : groups) {
            Set<String> functionsForType = functionsByGroup.get(group);
            if (functionsForType != null) {
                result.addAll(functionsForType);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    private @NotNull Map<DQLCommandGroup, Set<DQLCommandDefinition>> reloadCommandsByType() {
        Map<String, DQLCommandDefinition> commands = getCommands();
        Map<DQLCommandGroup, Set<DQLCommandDefinition>> result = new HashMap<>();
        for (DQLCommandDefinition command : commands.values()) {
            DQLCommandGroup commandGroup = command.getCommandGroup();
            if (commandGroup == null) {
                logger.warn("An unknown group " + command.type + " for a command " + command.name);
                continue;
            }
            result.putIfAbsent(commandGroup, new HashSet<>());
            result.get(commandGroup).add(command);
        }

        return Collections.unmodifiableMap(result);
    }

    private @NotNull Map<String, Set<DQLFunctionDefinition>> reloadFunctionsByType() {
        Map<String, DQLFunctionDefinition> functions = getFunctions();
        Map<String, Set<DQLFunctionDefinition>> result = new HashMap<>();
        for (DQLFunctionDefinition function : functions.values()) {
            if (function.returns != null) {
                for (String funcReturn : DQLDataType.getAllTypes(function.getDQLTypes())) {
                    result.putIfAbsent(funcReturn, new HashSet<>());
                    result.get(funcReturn).add(function);
                }
            }
        }

        return Collections.unmodifiableMap(result);
    }

    private @NotNull Map<DQLFunctionGroup, Set<String>> reloadFunctionsByGroup() {
        Map<String, DQLFunctionDefinition> functions = getFunctions();
        Map<DQLFunctionGroup, Set<String>> result = new HashMap<>();
        result.put(DQLFunctionGroup.RECORDS_LIST, Set.of("record"));
        for (DQLFunctionDefinition function : functions.values()) {
            DQLFunctionGroup functionGroup = function.getFunctionGroup();
            if (functionGroup == null) {
                logger.warn("An unknown group " + function.group + " for a function " + function.name);
                continue;
            }
            result.putIfAbsent(functionGroup, new HashSet<>());
            result.get(functionGroup).add(function.name);
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public void dispose() {
        dqlCommands = null;
        dqlFunctions = null;
    }
}

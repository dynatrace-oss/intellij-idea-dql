package pl.thedeem.intellij.dql.definition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DQLFunctionsLoader {
    private static final String TIMEFRAME_FUNCTIONS_PARAMETERS_PATH = "/dql/timeseries-functions-params.json";
    private static final String FUNCTION_DEFINITION_FILE = "/dql/functions/%s.json";

    private static Map<String, Set<DQLFunctionDefinition>> functionsByType = Map.of();
    private static Map<DQLFunctionGroup, Set<String>> functionsByGroup = Map.of();
    private static final Map<String, DQLFunctionDefinition> functions = loadFunctions();
    private static final DQLFunctionDefinition timeseriesParameters = loadTimeseriesParams();

    private static Map<String, DQLFunctionDefinition> loadFunctions() {
        Map<String, DQLFunctionDefinition> result = new HashMap<>();
        functionsByType = new HashMap<>();
        functionsByGroup = new HashMap<>();
        functionsByGroup.put(DQLFunctionGroup.RECORDS_LIST, Set.of("record"));
        for (DQLFunctionGroup group : DQLFunctionGroup.values()) {
            if (group.isPhony()) {
                continue;
            }
            String filePath = String.format(FUNCTION_DEFINITION_FILE, group.getName());
            try (InputStream inputStream = DQLFunctionsLoader.class.getResourceAsStream(filePath)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Function definitions file not found: " + filePath);
                }
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<Map<String, DQLFunctionDefinition>> typeRef = new TypeReference<>() {};
                Map<String, DQLFunctionDefinition> r = mapper.readValue(inputStream, typeRef);
                for (DQLFunctionDefinition value : r.values()) {
                    DQLFunctionGroup functionGroup = DQLFunctionGroup.getGroup(value.group);
                    if (functionGroup == null) {
                        System.err.println("Unknown command group: " + value.group + " for function " + value.name);
                        functionGroup = group;
                    }
                    if (value.aliases != null) {
                        for (String alias : value.aliases) {
                            DQLFunctionDefinition aliased = value.clone(alias);
                            result.put(alias.toLowerCase(), aliased);
                            registerFunctionType(aliased);
                            functionsByGroup.putIfAbsent(functionGroup, new HashSet<>());
                            functionsByGroup.get(functionGroup).add(aliased.name);
                        }
                    } else {
                        DQLFunctionDefinition clone = value.clone(value.name);
                        result.put(value.name.toLowerCase(), clone);
                        registerFunctionType(clone);
                        functionsByGroup.putIfAbsent(functionGroup, new HashSet<>());
                        functionsByGroup.get(functionGroup).add(clone.name);
                    }
                }
            } catch (IOException ignored) {
                System.err.println("Failed to load function definitions from " + filePath);
            }
        }
        return result;
    }

    private static DQLFunctionDefinition loadTimeseriesParams() {
        DQLFunctionDefinition result = null;
        try (InputStream inputStream = DQLFunctionsLoader.class.getResourceAsStream(TIMEFRAME_FUNCTIONS_PARAMETERS_PATH)) {
            ObjectMapper mapper = new ObjectMapper();
            result = mapper.readValue(inputStream, DQLFunctionDefinition.class);
        } catch (IOException ignored) {
            System.err.println("Failed to load function definitions from " + TIMEFRAME_FUNCTIONS_PARAMETERS_PATH);
        }
        return result;
    }

    private static void registerFunctionType(DQLFunctionDefinition function) {
        if (function.returns != null) {
            for (String funcReturn : DQLDataType.getAllTypes(function.getDQLTypes())) {
                functionsByType.putIfAbsent(funcReturn, new HashSet<>());
                functionsByType.get(funcReturn).add(function);
            }
        }
    }

    public static Map<String, DQLFunctionDefinition> getFunctions() {
        return functions;
    }

    public static DQLFunctionDefinition getFunction(String functionName) {
        return functionName != null ? functions.get(functionName.toLowerCase()) : null;
    }

    public static Set<DQLFunctionDefinition> getFunctionByNames(Set<String> functionName) {
        Set<DQLFunctionDefinition> result = new HashSet<>();
        for (String name : functionName) {
            DQLFunctionDefinition definition = functions.get(name.toLowerCase());
            if (definition != null) {
                result.add(definition);
            }
        }
        return result;
    }

    public static Set<DQLFunctionDefinition> getFunctionByTypes(Set<DQLDataType> types) {
        Set<DQLFunctionDefinition> result = new HashSet<>();
        for (String type : DQLDataType.getAllTypes(types)) {
            Set<DQLFunctionDefinition> functionsForType = functionsByType.get(type);
            if (functionsForType != null) {
                result.addAll(functionsForType);
            }
        }

        return result;
    }

    public static Set<String> getFunctionNamesByGroups(Set<DQLFunctionGroup> groups) {
        Set<String> result = new HashSet<>();
        for (DQLFunctionGroup group : groups) {
            Set<String> functionsForType = functionsByGroup.get(group);
            if (functionsForType != null) {
                result.addAll(functionsForType);
            }
        }

        return result;
    }

    public static List<DQLParameterDefinition> getTimeseriesParams() {
        return timeseriesParameters != null ? timeseriesParameters.parameters : List.of();
    }
}

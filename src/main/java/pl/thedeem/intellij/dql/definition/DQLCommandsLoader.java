package pl.thedeem.intellij.dql.definition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DQLCommandsLoader {
    private static final String COMMAND_DEFINITION_FILE = "/dql/commands/%s.json";

    private static Map<DQLCommandGroup, Set<DQLCommandDefinition>> commandsByType = Map.of();
    private static final Map<String, DQLCommandDefinition> commands = loadCommands();


    private static Map<String, DQLCommandDefinition> loadCommands() {
        Map<String, DQLCommandDefinition> result = new HashMap<>();
        commandsByType = new HashMap<>();
        for (DQLCommandGroup group : DQLCommandGroup.values()) {
            String filePath = String.format(COMMAND_DEFINITION_FILE, group.getName());
            try (InputStream inputStream = DQLCommandsLoader.class.getResourceAsStream(filePath)) {
                if (inputStream == null) {
                    throw new FileNotFoundException("Commands definitions file not found: " + filePath);
                }
                ObjectMapper mapper = new ObjectMapper();
                TypeReference<Map<String, DQLCommandDefinition>> typeRef = new TypeReference<>() {
                };
                Map<String, DQLCommandDefinition> r = mapper.readValue(inputStream, typeRef);
                for (DQLCommandDefinition value : r.values()) {
                    value.initialize();
                    result.put(value.name.toLowerCase(), value);
                    DQLCommandGroup commandGroup = DQLCommandGroup.getGroup(value.type);
                    if (commandGroup == null) {
                        System.err.println("Unknown command group: " + value.type + " for command " + value.name);
                        commandGroup = group;
                    }
                    commandsByType.putIfAbsent(commandGroup, new HashSet<>());
                    commandsByType.get(commandGroup).add(value);
                }
            } catch (IOException ignored) {
                System.err.println("Failed to load command definitions from " + filePath);
            }
        }
        return result;
    }

    public static Map<String, DQLCommandDefinition> getCommands() {
        return commands;
    }

    public static DQLCommandDefinition getCommand(String commandName) {
        return commandName != null ? commands.get(commandName.toLowerCase()) : null;
    }

    public static DQLCommandDefinition getCommand(@NotNull DQLQueryStatement command) {
        String commandName = command.getName();
        return commandName != null ? commands.get(commandName.toLowerCase()) : null;
    }

    public static List<DQLCommandDefinition> getStartingCommand() {
        List<DQLCommandDefinition> result = new ArrayList<>();
        for (DQLCommandGroup startingCommandType : DQLCommandGroup.STARTING_COMMAND_TYPES) {
            result.addAll(commandsByType.getOrDefault(startingCommandType, Set.of()));
        }
        return result;
    }

    public static List<DQLCommandDefinition> getExtensionCommand() {
        List<DQLCommandDefinition> result = new ArrayList<>();
        for (DQLCommandGroup startingCommandType : DQLCommandGroup.EXTENSION_COMMAND_TYPES) {
            result.addAll(commandsByType.getOrDefault(startingCommandType, Set.of()));
        }
        return result;
    }
}

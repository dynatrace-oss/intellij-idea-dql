package pl.thedeem.intellij.dql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.*;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DQLTestsUtils {
   public static Map<String, DQLCommandDefinition> createMockedCommands(@NotNull List<DQLCommandDefinition> commands) {
      return commands.stream().collect(Collectors.toMap(DQLCommandDefinition::getName, Function.identity()));
   }

   public static DQLCommandDefinition createCommand(
       @NotNull String name,
       @NotNull DQLCommandGroup type,
       @NotNull List<DQLParameterDefinition> parameters
   ) {
      DQLCommandDefinition command = new DQLCommandDefinition();
      command.name = name;
      command.type = type.getName();
      command.description = "empty description";
      command.parameters = parameters;
      command.initialize();
      return command;
   }

   public static Map<String, DQLFunctionDefinition> createMockedFunctions(@NotNull List<DQLFunctionDefinition> commands) {
      return commands.stream().collect(Collectors.toMap(DQLFunctionDefinition::getName, Function.identity()));
   }

   public static DQLFunctionDefinition createFunction(
       @NotNull String name,
       @NotNull DQLFunctionGroup type,
       @NotNull List<DQLDataType> returnedValues,
       @NotNull List<DQLParameterDefinition> parameters
   ) {
      DQLFunctionDefinition function = new DQLFunctionDefinition();
      function.name = name;
      function.group = type.getName();
      function.description = "empty description";
      function.longDescription = "empty description";
      function.parameters = List.of();
      function.returns = returnedValues.stream().map(DQLDataType::getName).collect(Collectors.toList());
      function.parameters = parameters;
      function.initialize();
      return function;
   }

   public static DQLParameterDefinition createParameter(
       @NotNull String name,
       @NotNull List<DQLDataType> types,
       boolean required,
       @NotNull List<String> disallows,
       @NotNull List<String> enumValues,
       @NotNull List<String> suggested,
       @Nullable String defaultValue,
       boolean nameAllowed
   ) {
      DQLParameterDefinition parameter = createParameter(name, types);
      parameter.required = required;
      parameter.disallows = disallows;
      parameter.enumValues = enumValues;
      parameter.suggested = suggested;
      parameter.defaultValue = defaultValue;
      parameter.nameAllowed = nameAllowed;
      return parameter;
   }

   public static DQLParameterDefinition createParameter(
       @NotNull String name,
       @NotNull List<DQLDataType> types
   ) {
      DQLParameterDefinition parameter = new DQLParameterDefinition();
      parameter.name = name;
      parameter.description = "empty description";
      parameter.type = types.stream().map(DQLDataType::getName).collect(Collectors.toList());
      return parameter;
   }
}

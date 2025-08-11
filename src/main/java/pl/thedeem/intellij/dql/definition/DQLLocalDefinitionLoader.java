package pl.thedeem.intellij.dql.definition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DQLLocalDefinitionLoader implements DQLDefinitionLoader {
   private static final String COMMAND_DEFINITION_FILE = "/dql/commands/%s.json";
   private static final String TIME_SERIES_FUNCTIONS_PARAMETERS_PATH = "/dql/timeseries-functions-params.json";
   private static final String FUNCTION_DEFINITION_FILE = "/dql/functions/%s.json";
   private static final String OPERATOR_DEFINITION_FILE = "/dql/operators/%s.json";

   private static final ObjectMapper mapper = new ObjectMapper();
   private static final Logger logger = Logger.getInstance(DQLLocalDefinitionLoader.class);

   @Override
   public @NotNull Map<String, DQLCommandDefinition> loadCommands() {
      logger.info("Reloading commands...");
      Map<String, DQLCommandDefinition> result = new HashMap<>();
      for (DQLCommandGroup group : DQLCommandGroup.values()) {
         String path = String.format(COMMAND_DEFINITION_FILE, group.getName());
         Map<String, DQLCommandDefinition> command = loadDefinition(path, new TypeReference<>() {
         });
         if (command != null) {
            for (DQLCommandDefinition value : command.values()) {
               value.initialize();
               result.put(value.name.toLowerCase(), value);
            }
         }
      }
      return Collections.unmodifiableMap(result);
   }

   @Override
   public @NotNull Map<String, DQLFunctionDefinition> loadFunctions() {
      logger.info("Reloading functions...");
      Map<String, DQLFunctionDefinition> result = new HashMap<>();
      for (DQLFunctionGroup group : DQLFunctionGroup.values()) {
         if (group.isPhony()) {
            continue;
         }
         String filePath = String.format(FUNCTION_DEFINITION_FILE, group.getName());
         Map<String, DQLFunctionDefinition> function = loadDefinition(filePath, new TypeReference<>() {
         });
         if (function != null) {
            for (DQLFunctionDefinition value : function.values()) {
               if (value.aliases != null) {
                  for (String alias : value.aliases) {
                     DQLFunctionDefinition aliased = value.clone(alias);
                     result.put(alias.toLowerCase(), aliased);
                  }
               } else {
                  DQLFunctionDefinition clone = value.clone(value.name);
                  result.put(value.name.toLowerCase(), clone);
               }
            }
         }
      }
      return Collections.unmodifiableMap(result);
   }

   @Override
   public @NotNull List<DQLParameterDefinition> loadTimeSeriesParameters() {
      logger.info("Reloading time series parameters...");
      List<DQLParameterDefinition> result = new ArrayList<>();
      DQLFunctionDefinition timeSeriesParamsDefinition = loadDefinition(TIME_SERIES_FUNCTIONS_PARAMETERS_PATH, new TypeReference<>() {
      });
      if (timeSeriesParamsDefinition != null) {
         result.addAll(timeSeriesParamsDefinition.parameters);
      }
      return Collections.unmodifiableList(result);
   }

   @Override
   public @NotNull Map<IElementType, DQLOperationTarget> loadOperations() {
      logger.info("Reloading operations...");
      Map<IElementType, DQLOperationTarget> result = new HashMap<>();
      for (String operator : DQLOperationTarget.ALL) {
         String path = String.format(OPERATOR_DEFINITION_FILE, operator);
         DQLOperationTarget target = loadDefinition(path, new TypeReference<>() {
         });
         if (target != null) {
            target.initialize();
            for (IElementType type : target.getOperatorType()) {
               result.put(type, target);
            }
         }
      }
      return Collections.unmodifiableMap(result);
   }

   private @Nullable <T> T loadDefinition(@NotNull String path, @NotNull TypeReference<T> typeRef) {
      try (InputStream inputStream = getClass().getResourceAsStream(path)) {
         if (inputStream == null) {
            throw new FileNotFoundException("Definition file not found: " + path);
         }
         return mapper.readValue(inputStream, typeRef);
      } catch (IOException ignored) {
         logger.warn("Failed to load command definitions from " + path);
      }
      return null;
   }
}

package pl.thedeem.intellij.dql.definition;

import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.elements.impl.ExpressionOperatorImpl;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.*;

public interface DQLDefinitionService {
   static @NotNull DQLDefinitionService getInstance(@NotNull Project project) {
      return project.getService(DQLDefinitionService.class);
   }

   void invalidateCache();

   @NotNull Map<String, DQLCommandDefinition> getCommands();

   @NotNull Map<String, DQLFunctionDefinition> getFunctions();

   @NotNull List<DQLParameterDefinition> getTimeSeriesParameters();

   @NotNull Map<IElementType, DQLOperationTarget> getOperations();

   @NotNull Map<DQLCommandGroup, Set<DQLCommandDefinition>> getCommandsByType();

   @NotNull Map<String, Set<DQLFunctionDefinition>> getFunctionsByType();

   @NotNull Map<DQLFunctionGroup, Set<String>> getFunctionsByGroup();

   @Nullable DQLCommandDefinition getCommand(@NotNull DQLQueryStatement command);

   @Nullable DQLCommandDefinition getCommand(@Nullable String commandName);

   @Nullable DQLFunctionDefinition getFunction(@Nullable String functionName);

   @NotNull Set<DQLDataType> getResultType(@Nullable ExpressionOperatorImpl operator, @Nullable DQLExpression left, @Nullable DQLExpression right);

   @Nullable DQLOperationTarget getTargetType(@Nullable ExpressionOperatorImpl operator);

   @NotNull Set<DQLDataType> getResultType(@NotNull IElementType operator, @NotNull Set<DQLDataType> left, @NotNull Set<DQLDataType> right);

   @NotNull List<DQLCommandDefinition> getStartingCommands();

   @NotNull List<DQLCommandDefinition> getExtensionCommands();

   @NotNull Set<DQLFunctionDefinition> getFunctionByNames(@NotNull Set<String> functionName);

   @NotNull Set<DQLFunctionDefinition> getFunctionByTypes(@NotNull Set<DQLDataType> types);

   @NotNull Set<String> getFunctionNamesByGroups(@NotNull Set<DQLFunctionGroup> groups);
}

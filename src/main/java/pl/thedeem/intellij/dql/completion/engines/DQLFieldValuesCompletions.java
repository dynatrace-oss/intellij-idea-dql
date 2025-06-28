package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.definition.*;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DQLFieldValuesCompletions {
   public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
      PsiElement position = parameters.getPosition();
      List<PsiElement> parent = DQLUtil.getElementsUntilParent(position, DQLFunctionCallExpression.class, DQLQueryStatement.class);
      if (parent.getFirst() instanceof DQLFunctionCallExpression function && parent.get(1) instanceof DQLExpression expression) {
         autocompleteParameter(function.getParameter(expression), result);
      } else if (parent.getFirst() instanceof DQLQueryStatement list && parent.get(1) instanceof DQLExpression expression) {
         autocompleteParameter(list.getParameter(expression), result);
      }
   }

   private void autocompleteParameter(DQLParameterObject parameter, CompletionResultSet result) {
      if (parameter == null || parameter.getDefinition() == null) {
         return;
      }

      DQLParameterDefinition paramDefinition = parameter.getDefinition();
      if (parameter.isMissingName()) {
         return;
      }
      if (paramDefinition.enumValues != null && !paramDefinition.enumValues.isEmpty()) {
         for (String enumValue : paramDefinition.enumValues) {
            AutocompleteUtils.autocompleteStaticValue(enumValue, result);
         }
      } else {
         if (paramDefinition.suggested != null) {
            for (String suggestedValue : paramDefinition.suggested) {
               AutocompleteUtils.autocompleteStaticValue(suggestedValue, result);
            }
         }
         suggestValues(paramDefinition.getDQLTypes(), result);
      }
   }

   private void suggestValues(Set<DQLDataType> types, CompletionResultSet result) {
      if (types == null) {
         types = Set.of();
      }
      Collection<DQLFunctionDefinition> availableFunctions;
      if (types.contains(DQLDataType.RECORDS_LIST)) {
         Set<String> list = DQLFunctionsLoader.getFunctionNamesByGroups(Set.of(DQLFunctionGroup.RECORDS_LIST));
         availableFunctions = DQLFunctionsLoader.getFunctionByNames(list);
      } else if (types.contains(DQLDataType.AGGREGATION_FUNCTION)) {
         Set<String> list = DQLFunctionsLoader.getFunctionNamesByGroups(Set.of(DQLFunctionGroup.AGGREGATE));
         availableFunctions = DQLFunctionsLoader.getFunctionByNames(list);
      } else if (types.isEmpty() || types.contains(DQLDataType.ANY) || types.contains(DQLDataType.EXPRESSION)) {
         availableFunctions = DQLFunctionsLoader.getFunctions().values();
      } else {
         availableFunctions = DQLFunctionsLoader.getFunctionByTypes(types);
      }
      for (DQLFunctionDefinition function : availableFunctions) {
         AutocompleteUtils.autocompleteFunction(function, result);
      }
      if (types.isEmpty() || types.contains(DQLDataType.BOOLEAN) || types.contains(DQLDataType.ANY)) {
         AutocompleteUtils.autocompleteBooleans(result);
      }
      if (types.contains(DQLDataType.TIMESTAMP)) {
         AutocompleteUtils.autocompleteCurrentTimestamp(result);
      }
   }
}

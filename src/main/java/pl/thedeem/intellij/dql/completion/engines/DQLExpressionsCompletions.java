package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFunctionDefinition;
import pl.thedeem.intellij.dql.definition.DQLFunctionsLoader;
import pl.thedeem.intellij.dql.psi.*;

public class DQLExpressionsCompletions {
   public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
      PsiElement position = parameters.getPosition();
      if (position.getParent().getParent() instanceof DQLExpression expr) {
         autocompleteExpression(expr, result);
      }
   }

   private void autocompleteExpression(DQLExpression expression, CompletionResultSet result) {
      switch (expression) {
         case DQLAdditiveExpression ignored -> {
            for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.NUMERICAL_TYPES)) {
               AutocompleteUtils.autocompleteFunction(function, result);
            }
         }
         case DQLMultiplicativeExpression ignored -> {
            for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.NUMERICAL_TYPES)) {
               AutocompleteUtils.autocompleteFunction(function, result);
            }
         }
         case DQLConditionExpression ignored -> {
            for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.BOOLEAN_TYPES)) {
               AutocompleteUtils.autocompleteFunction(function, result);
            }
            AutocompleteUtils.autocompleteBooleans(result);
         }
         case DQLComparisonExpression ignored -> {
            for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.COMPARABLE_TYPES)) {
               AutocompleteUtils.autocompleteFunction(function, result);
            }
         }
         case DQLUnaryExpression ignored -> {
            for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.BOOLEAN_TYPES)) {
               AutocompleteUtils.autocompleteFunction(function, result);
            }
            AutocompleteUtils.autocompleteBooleans(result);
         }
         default -> {
         }
      }
   }
}

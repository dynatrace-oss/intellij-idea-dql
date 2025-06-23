package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.completion.DQLPsiPatterns;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFunctionDefinition;
import pl.thedeem.intellij.dql.definition.DQLFunctionsLoader;
import pl.thedeem.intellij.dql.psi.*;

public class DQLExpressionsCompletions implements DQLCompletionEngine {
    @Override
    public CompletionResult autocomplete(@NotNull PsiElement position, @NotNull CompletionResultSet result) {
        if (DQLPsiPatterns.SUGGEST_FIELD_VALUES.accepts(position)) {
            if (position.getParent().getParent() instanceof DQLExpression expr) {
                return autocompleteExpression(expr, result);
            }
        }
        return CompletionResult.PASS;
    }

    private CompletionResult autocompleteExpression(DQLExpression expression, CompletionResultSet result) {
        return switch (expression) {
            case DQLAdditiveExpression ignored -> {
                for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.NUMERICAL_TYPES)) {
                    AutocompleteUtils.autocompleteFunction(function, result);
                }
                yield CompletionResult.STOP;
            }
            case DQLMultiplicativeExpression ignored -> {
                for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.NUMERICAL_TYPES)) {
                    AutocompleteUtils.autocompleteFunction(function, result);
                }
                yield CompletionResult.STOP;
            }
            case DQLConditionExpression ignored -> {
                for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.BOOLEAN_TYPES)) {
                    AutocompleteUtils.autocompleteFunction(function, result);
                }
                AutocompleteUtils.autocompleteBooleans(result);
                yield CompletionResult.STOP;
            }
            case DQLComparisonExpression ignored -> {
                for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.COMPARABLE_TYPES)) {
                    AutocompleteUtils.autocompleteFunction(function, result);
                }
                yield CompletionResult.STOP;
            }
            case DQLUnaryExpression ignored -> {
                for (DQLFunctionDefinition function : DQLFunctionsLoader.getFunctionByTypes(DQLDataType.BOOLEAN_TYPES)) {
                    AutocompleteUtils.autocompleteFunction(function, result);
                }
                AutocompleteUtils.autocompleteBooleans(result);
                yield CompletionResult.STOP;
            }
            default -> CompletionResult.PASS;
        };
    }
}

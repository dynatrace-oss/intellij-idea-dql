package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.psi.*;

public class DQLExpressionsCompletions {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (position.getParent().getParent() instanceof DQLExpression expr) {
            autocompleteExpression(expr, result);
        }
    }

    private void autocompleteExpression(DQLExpression expression, CompletionResultSet result) {
        DQLDefinitionService service = DQLDefinitionService.getInstance(expression.getProject());
        switch (expression) {
            case DQLArithmeticalExpression ignored -> {
                for (Function function : service.getFunctionsByReturnType(DQLDefinitionService.NUMERICAL_TYPES)) {
                    AutocompleteUtils.autocomplete(function, result);
                }
            }
            case DQLConditionExpression ignored -> {
                for (Function function : service.getFunctionsByReturnType(DQLDefinitionService.BOOLEAN_TYPES)) {
                    AutocompleteUtils.autocomplete(function, result);
                }
                AutocompleteUtils.autocompleteBooleans(result);
            }
            case DQLComparisonExpression ignored -> {
                for (Function function : service.getFunctionsByReturnType(DQLDefinitionService.COMPARABLE_TYPES)) {
                    AutocompleteUtils.autocomplete(function, result);
                }
                AutocompleteUtils.autocompleteBooleans(result);
            }
            case DQLUnaryExpression ignored -> {
                for (Function function : service.getFunctionsByReturnType(DQLDefinitionService.BOOLEAN_TYPES)) {
                    AutocompleteUtils.autocomplete(function, result);
                }
                AutocompleteUtils.autocompleteBooleans(result);
            }
            default -> {
            }
        }
    }
}

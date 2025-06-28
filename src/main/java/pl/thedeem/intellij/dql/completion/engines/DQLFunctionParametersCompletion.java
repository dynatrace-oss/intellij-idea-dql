package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.completion.DQLPsiPatterns;
import pl.thedeem.intellij.dql.definition.DQLFunctionDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DQLFunctionParametersCompletion implements DQLCompletionEngine {
    @Override
    public CompletionResult autocomplete(@NotNull CompletionParameters parameters, @NotNull PsiElement position, @NotNull CompletionResultSet result) {
        if (DQLPsiPatterns.SUGGEST_FUNCTION_PARAMETERS.accepts(position)) {
            List<PsiElement> parents = DQLUtil.getElementsUntilParent(position, DQLFunctionCallExpression.class);
            if (parents.getFirst() instanceof DQLFunctionCallExpression function) {
                DQLFunctionDefinition definition = function.getDefinition();
                if (definition != null) {
                    Set<String> functionParameters = function.getParameters().stream()
                            .filter(p -> p.getDefinition() != null)
                            .filter(p -> !CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED.equals(p.getExpression().getText()))
                            .map(p -> p.getDefinition().name)
                            .collect(Collectors.toSet());
                    Set<DQLParameterDefinition> available = definition
                            .getParameters(function)
                            .stream()
                            .filter(p -> !functionParameters.contains(p.name))
                            .collect(Collectors.toSet());

                    for (DQLParameterDefinition param : available) {
                        AutocompleteUtils.autocompleteParameter(param, result, !functionParameters.isEmpty());
                    }
                    if (!available.isEmpty()) {
                        return CompletionResult.STOP;
                    }
                }
            }
        }
        return CompletionResult.PASS;
    }
}

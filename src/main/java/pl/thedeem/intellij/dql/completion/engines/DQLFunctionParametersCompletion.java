package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DQLFunctionParametersCompletion {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        List<PsiElement> parents = PsiUtils.getElementsUntilParent(position, DQLFunctionExpression.class);
        if (parents.getFirst() instanceof DQLFunctionExpression function) {
            Function definition = function.getDefinition();
            Signature signature = function.getSignature();
            if (definition == null || signature == null) {
                return;
            }
            Set<String> functionParameters = function.getParameters().stream()
                    .filter(p -> p.definition() != null)
                    .filter(p -> !CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED.equals(p.holder().getText()))
                    .map(p -> p.definition().name())
                    .collect(Collectors.toSet());
            Set<Parameter> available = signature
                    .parameters()
                    .stream()
                    .filter(p -> !functionParameters.contains(p.name()))
                    .collect(Collectors.toSet());

            for (Parameter param : available) {
                AutocompleteUtils.autocomplete(param, result, !functionParameters.isEmpty());
            }
        }
    }
}

package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;

import java.util.List;

public class DQLStatementParamsCompletion {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        List<PsiElement> parents = PsiUtils.getElementsUntilParent(position, DQLQueryStatement.class);
        if (!(parents.size() > 1 && parents.getFirst() instanceof DQLQueryStatement statement)) {
            return;
        }
        Command definition = statement.getDefinition();
        MappedParameter currentParameter = parents.get(1) instanceof DQLExpression expression ? statement.getParameter(expression) : null;
        if (definition != null) {
            List<MappedParameter> definedParams = statement.getParameters();
            boolean addComma = !definedParams.isEmpty() && !CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED.equals(definedParams.getFirst().holder().getText());
            for (Parameter parameter : statement.getMissingParameters()) {
                AutocompleteUtils.autocomplete(parameter, result, addComma);
            }
            if (currentParameter != null && currentParameter.definition() != null) {
                AutocompleteUtils.autocomplete(currentParameter.definition(), result, addComma);
            }
        }
    }
}

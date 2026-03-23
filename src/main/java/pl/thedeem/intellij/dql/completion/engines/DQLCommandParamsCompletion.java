package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.services.definition.model.Command;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;

import java.util.List;

public class DQLCommandParamsCompletion {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        List<PsiElement> parents = PsiUtils.getElementsUntilParent(position, DQLCommand.class);
        if (parents.isEmpty() || !(parents.getFirst() instanceof DQLCommand statement)) {
            return;
        }
        Command definition = statement.getDefinition();
        MappedParameter currentParameter = parents.size() > 1 && parents.get(1) instanceof DQLExpression expression ? statement.getParameter(expression) : null;
        if (definition != null) {
            boolean addComma = statement.getChildren().length > 2;
            for (Parameter parameter : statement.getMissingParameters()) {
                AutocompleteUtils.autocomplete(parameter, result, addComma);
            }
            if (currentParameter != null && currentParameter.definition() != null) {
                AutocompleteUtils.autocomplete(currentParameter.definition(), result, addComma);
            }
        }
    }
}

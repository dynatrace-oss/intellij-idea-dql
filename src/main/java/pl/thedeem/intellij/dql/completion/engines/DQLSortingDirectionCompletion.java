package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLExpression;

import java.util.List;

public class DQLSortingDirectionCompletion {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (position.getParent() instanceof PsiErrorElement error) {
            PsiElement previous = PsiUtils.getPreviousElement(error);
            if (previous != null) {
                List<PsiElement> parents = PsiUtils.getElementsUntilParent(previous, DQLCommand.class);
                if (parents.size() > 1
                        && parents.getFirst() instanceof DQLCommand statement
                        && parents.get(1) instanceof DQLExpression expression) {
                    MappedParameter parameter = statement.getParameter(expression);
                    Parameter definition = parameter != null ? parameter.definition() : null;
                    if (definition != null && definition.allowsSorting()) {
                        AutocompleteUtils.autocompleteSortingKeyword(result);
                    }
                }
            }
        }
    }
}

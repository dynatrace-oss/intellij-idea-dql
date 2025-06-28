package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DQLSortingDirectionCompletion implements DQLCompletionEngine {
    @Override
    public CompletionResult autocomplete(@NotNull CompletionParameters parameters, @NotNull PsiElement position, @NotNull CompletionResultSet result) {
        if (position.getParent() instanceof PsiErrorElement error) {
            PsiElement previous = DQLUtil.getPreviousElement(error);
            if (previous != null) {
                List<PsiElement> parents = DQLUtil.getElementsUntilParent(previous, DQLQueryStatement.class);
                if (parents.size() > 1
                        && parents.getFirst() instanceof DQLQueryStatement statement
                        && parents.get(1) instanceof DQLExpression expression) {
                    DQLParameterObject parameter = statement.getParameter(expression);
                    if (isSortingAllowed(parameter)) {
                        AutocompleteUtils.autocompleteSortingKeyword(result);
                    }
                }
            }
        }
        return CompletionResult.PASS;
    }

    private boolean isSortingAllowed(DQLParameterObject parameter) {
        return parameter != null && parameter.getDefinition() != null && parameter.getDefinition().getDQLTypes().contains(DQLDataType.SORTING_EXPRESSION);
    }
}

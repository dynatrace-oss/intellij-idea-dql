package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.completion.DQLPsiPatterns;
import org.jetbrains.annotations.NotNull;

public class DQLInExpressionCompletion implements DQLCompletionEngine {
    @Override
    public CompletionResult autocomplete(@NotNull CompletionParameters parameters, @NotNull PsiElement position, @NotNull CompletionResultSet result) {
        if (DQLPsiPatterns.SIBLING_OF_FIELD.accepts(position)) {
            AutocompleteUtils.autocompleteInExpression(result);
        }
        return CompletionResult.PASS;
    }
}

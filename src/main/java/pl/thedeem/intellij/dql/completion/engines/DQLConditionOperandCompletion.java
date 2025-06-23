package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.completion.DQLPsiPatterns;
import org.jetbrains.annotations.NotNull;

public class DQLConditionOperandCompletion implements DQLCompletionEngine {
    @Override
    public CompletionResult autocomplete(@NotNull PsiElement position, @NotNull CompletionResultSet result) {
        if (DQLPsiPatterns.SUGGEST_CONDITION_OPERANDS.accepts(position)) {
            AutocompleteUtils.autocompleteConditionOperands(result);
        }
        return CompletionResult.PASS;
    }
}

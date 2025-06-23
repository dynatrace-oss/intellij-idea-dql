package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface DQLCompletionEngine {
    CompletionResult autocomplete(@NotNull PsiElement position, @NotNull CompletionResultSet result);

    enum CompletionResult {
        PASS,
        STOP,
        FORCE_STOP
    }
}

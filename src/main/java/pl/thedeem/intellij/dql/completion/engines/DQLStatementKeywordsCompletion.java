package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.completion.DQLPsiPatterns;
import pl.thedeem.intellij.dql.definition.DQLCommandDefinition;
import pl.thedeem.intellij.dql.definition.DQLCommandsLoader;
import org.jetbrains.annotations.NotNull;

public class DQLStatementKeywordsCompletion implements DQLCompletionEngine {
    @Override
    public CompletionResult autocomplete(@NotNull CompletionParameters parameters, @NotNull PsiElement position, @NotNull CompletionResultSet result) {
        if (DQLUtil.isPartialFile(position.getContainingFile())) {
            if (DQLPsiPatterns.SUGGEST_QUERY_START.accepts(position)) {
                for (DQLCommandDefinition command : DQLCommandsLoader.getCommands().values()) {
                    AutocompleteUtils.autocompleteStatement(command, AutocompleteUtils.COMMAND, result);
                }
            }
            else if (DQLPsiPatterns.QUERY_COMMAND.accepts(position)) {
                for (DQLCommandDefinition command : DQLCommandsLoader.getExtensionCommand()) {
                    AutocompleteUtils.autocompleteStatement(command, AutocompleteUtils.COMMAND, result);
                }
                return CompletionResult.PASS;
            }
        }
        else if (DQLPsiPatterns.SUGGEST_QUERY_START.accepts(position)) {
            for (DQLCommandDefinition command : DQLCommandsLoader.getStartingCommand()) {
                AutocompleteUtils.autocompleteStatement(command, AutocompleteUtils.QUERY_START, result);
            }
            return CompletionResult.FORCE_STOP;
        } else if (DQLPsiPatterns.QUERY_COMMAND.accepts(position)) {
            for (DQLCommandDefinition command : DQLCommandsLoader.getExtensionCommand()) {
                AutocompleteUtils.autocompleteStatement(command, AutocompleteUtils.COMMAND, result);
            }
            return CompletionResult.PASS;
        }

        return CompletionResult.PASS;
    }
}

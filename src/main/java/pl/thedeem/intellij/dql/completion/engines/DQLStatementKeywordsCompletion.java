package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.completion.DQLPsiPatterns;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Command;

public class DQLStatementKeywordsCompletion {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        DQLDefinitionService service = DQLDefinitionService.getInstance(position.getProject());

        if (DQLUtil.isPartialFile(position.getContainingFile())) {
            if (DQLPsiPatterns.SUGGEST_QUERY_START.accepts(position)) {
                for (Command command : service.getCommands()) {
                    AutocompleteUtils.autocomplete(command, result);
                }
            } else if (DQLPsiPatterns.QUERY_COMMAND.accepts(position)) {
                for (Command command : service.getCommandsByCategory(DQLDefinitionService.EXTENSION_COMMANDS)) {
                    AutocompleteUtils.autocomplete(command, result);
                }
            }
        } else if (DQLPsiPatterns.SUGGEST_QUERY_START.accepts(position)) {
            for (Command command : service.getCommandsByCategory(DQLDefinitionService.STARTING_COMMANDS)) {
                AutocompleteUtils.autocomplete(command, result);
            }
        } else if (DQLPsiPatterns.QUERY_COMMAND.accepts(position)) {
            for (Command command : service.getCommandsByCategory(DQLDefinitionService.EXTENSION_COMMANDS)) {
                AutocompleteUtils.autocomplete(command, result);
            }
        }
    }
}

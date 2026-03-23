package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.completion.DQLPsiPatterns;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.services.definition.model.Command;

import java.util.Collection;

public class DQLStatementKeywordsCompletion {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        DQLDefinitionService service = DQLDefinitionService.getInstance(position.getProject());
        Collection<String> dsCommands = service.getDataSourceCommands();
        if (DQLUtil.isPartialFile(position.getContainingFile())) {
            if (DQLPsiPatterns.SUGGEST_QUERY_START.accepts(position)) {
                for (Command command : service.getCommands()) {
                    AutocompleteUtils.autocomplete(command, result);
                }
            } else if (DQLPsiPatterns.QUERY_COMMAND.accepts(position)) {
                service.getCommands().stream()
                        .filter(command -> !dsCommands.contains(command.name()))
                        .forEach(command -> AutocompleteUtils.autocomplete(command, result));
            }
        } else if (DQLPsiPatterns.SUGGEST_QUERY_START.accepts(position)) {
            for (String commandName : dsCommands) {
                Command command = service.getCommandByName(commandName);
                if (command != null) {
                    AutocompleteUtils.autocomplete(command, result);
                }
            }
        } else if (DQLPsiPatterns.QUERY_COMMAND.accepts(position)) {
            service.getCommands().stream()
                    .filter(command -> !dsCommands.contains(command.name()))
                    .forEach(command -> AutocompleteUtils.autocomplete(command, result));
        }
    }
}

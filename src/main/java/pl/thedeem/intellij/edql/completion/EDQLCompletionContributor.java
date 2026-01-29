package pl.thedeem.intellij.edql.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.psi.DQLFieldName;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.edql.EDQLFileType;

public class EDQLCompletionContributor extends CompletionContributor {
    public EDQLCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(DQLFieldName.class),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        PsiElement position = parameters.getPosition();
                        if (!EDQLFileType.INSTANCE.equals(position.getContainingFile().getFileType())) {
                            return;
                        }
                        DQLDefinitionService service = DQLDefinitionService.getInstance(position.getProject());
                        for (Function function : service.getFunctions()) {
                            AutocompleteUtils.autocomplete(function, resultSet);
                        }
                        AutocompleteUtils.autocompleteBooleans(resultSet);
                        AutocompleteUtils.autocompleteStringValues(resultSet);
                    }
                }
        );
    }
}

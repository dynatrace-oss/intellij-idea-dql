package pl.thedeem.intellij.dpl.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.completion.providers.DPLCommandCompletions;
import pl.thedeem.intellij.dpl.completion.providers.DPLConfigurationParameterCompletion;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class DPLLocalCompletionContributor extends CompletionContributor {
    public DPLLocalCompletionContributor() {
        extend(CompletionType.BASIC, psiElement(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        new DPLCommandCompletions().autocomplete(parameters, resultSet);
                    }
                }
        );
        extend(CompletionType.BASIC, psiElement(),
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        new DPLConfigurationParameterCompletion().autocomplete(parameters, resultSet);
                    }
                }
        );
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        result = result.withRelevanceSorter(CompletionSorter.emptySorter().weigh(new DPLCompletionWeigher()));
        super.fillCompletionVariants(parameters, result);
    }
}

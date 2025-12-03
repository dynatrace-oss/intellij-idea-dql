package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.completion.engines.*;
import pl.thedeem.intellij.dql.psi.DQLTypes;

public class DQLLocalCompletionContributor extends CompletionContributor {
    public DQLLocalCompletionContributor() {
        extend(CompletionType.BASIC, DQLPsiPatterns.SUGGEST_FIELD_VALUES,
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        new DQLExpressionsCompletions().autocomplete(parameters, resultSet);
                    }
                }
        );
        extend(CompletionType.BASIC, DQLPsiPatterns.SUGGEST_FIELD_VALUES,
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        new DQLFieldValuesCompletions().autocomplete(parameters, resultSet);
                    }
                }
        );
        extend(CompletionType.BASIC, DQLPsiPatterns.INSIDE_STATEMENT_PARAMETERS_LIST,
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        new DQLStatementParamsCompletion().autocomplete(parameters, resultSet);
                    }
                }
        );
        extend(CompletionType.BASIC, DQLPsiPatterns.SUGGEST_FUNCTION_PARAMETERS,
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        new DQLFunctionParametersCompletion().autocomplete(parameters, resultSet);
                    }
                }
        );
        extend(CompletionType.BASIC, DQLPsiPatterns.SUGGEST_COMMAND_KEYWORDS,
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        new DQLStatementKeywordsCompletion().autocomplete(parameters, resultSet);
                    }
                }
        );
        extend(CompletionType.BASIC, DQLPsiPatterns.INSERTION_ERROR_ELEMENT,
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        new DQLConditionOperandCompletion().autocomplete(parameters, resultSet);
                    }
                }
        );
        extend(CompletionType.BASIC, DQLPsiPatterns.INSERTION_ERROR_ELEMENT,
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        new DQLSortingDirectionCompletion().autocomplete(parameters, resultSet);
                    }
                }
        );
        extend(CompletionType.BASIC, DQLPsiPatterns.SIBLING_OF_FIELD,
                new CompletionProvider<>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        AutocompleteUtils.autocompleteInExpression(resultSet);
                    }
                }
        );
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        result = result.withRelevanceSorter(CompletionSorter.emptySorter().weigh(new DQLCompletionWeigher()));
        PsiElement position = parameters.getPosition();

        // inside comments, we don't want any completions
        if (PlatformPatterns.or(
                PlatformPatterns.psiElement(DQLTypes.ML_COMMENT),
                PlatformPatterns.psiElement(DQLTypes.EOL_COMMENT)
        ).accepts(position)) {
            return;
        }

        super.fillCompletionVariants(parameters, result);
    }
}

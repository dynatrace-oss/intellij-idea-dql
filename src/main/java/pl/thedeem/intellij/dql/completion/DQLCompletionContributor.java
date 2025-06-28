package pl.thedeem.intellij.dql.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionSorter;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.completion.engines.*;
import pl.thedeem.intellij.dql.psi.DQLTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.patterns.PsiJavaPatterns.psiElement;

public class DQLCompletionContributor extends CompletionContributor {
   // The order is important
   private final static List<DQLCompletionEngine> ENGINES = List.of(
       new DQLLiveAutocomplete(),
       new DQLExpressionsCompletions(),
       new DQLStatementParamsCompletion(),
       new DQLFunctionParametersCompletion(),
       new DQLFieldValuesCompletions(),
       new DQLStatementKeywordsCompletion(),
       new DQLConditionOperandCompletion(),
       new DQLSortingDirectionCompletion(),
       new DQLInExpressionCompletion()
   );

   @Override
   public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
      result = result.withRelevanceSorter(CompletionSorter.emptySorter().weigh(new DQLCompletionWeigher()));
      PsiElement position = parameters.getPosition();

      // inside comments, we don't want any completions
      if (psiElement(DQLTypes.EOL_COMMENT).accepts(position)) {
         return;
      }

      for (DQLCompletionEngine engine : ENGINES) {
         DQLCompletionEngine.CompletionResult completionResult = engine.autocomplete(parameters, position, result);
         if (completionResult == DQLCompletionEngine.CompletionResult.STOP) {
            super.fillCompletionVariants(parameters, result);
            return;
         } else if (completionResult == DQLCompletionEngine.CompletionResult.FORCE_STOP) {
            result.stopHere();
            return;
         }
      }
   }
}

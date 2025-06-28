package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.Set;

import static com.intellij.patterns.PsiJavaPatterns.psiElement;

public class DQLConditionOperandCompletion {
   public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
      PsiElement position = parameters.getPosition();
      PsiElement parent = position.getParent();
      if (parent != null && TokenType.ERROR_ELEMENT == parent.getNode().getElementType()) {
         PsiElement neighbour = DQLUtil.getDeepNeighbourElement(parent, PlatformPatterns.or(
             psiElement(DQLBoolean.class),
             psiElement(DQLComparisonExpression.class),
             psiElement(DQLEqualityExpression.class),
             psiElement(DQLConditionExpression.class),
             psiElement(DQLFunctionCallExpression.class)
         ));
         if (neighbour instanceof BaseTypedElement typedElement) {
            Set<DQLDataType> dataType = typedElement.getDataType();
            if (dataType.contains(DQLDataType.BOOLEAN) || dataType.contains(DQLDataType.ANY)) {
               AutocompleteUtils.autocompleteConditionOperands(result);
            }
         } else if (neighbour != null) {
            AutocompleteUtils.autocompleteConditionOperands(result);
         }
      }
   }
}

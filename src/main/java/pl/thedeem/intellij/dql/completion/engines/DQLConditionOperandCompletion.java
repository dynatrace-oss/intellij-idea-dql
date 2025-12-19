package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.Collection;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class DQLConditionOperandCompletion {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        PsiElement parent = position.getParent();
        if (parent != null && TokenType.ERROR_ELEMENT == parent.getNode().getElementType()) {
            PsiElement neighbour = PsiUtils.getDeepNeighbourElement(parent, PlatformPatterns.or(
                    psiElement(DQLBoolean.class),
                    psiElement(DQLComparisonExpression.class),
                    psiElement(DQLEqualityExpression.class),
                    psiElement(DQLConditionExpression.class),
                    psiElement(DQLFunctionExpression.class)
            ));
            if (neighbour instanceof BaseTypedElement typedElement) {
                Collection<String> dataType = typedElement.getDataType();
                if (dataType.contains("dql.dataType.boolean")) {
                    AutocompleteUtils.autocompleteConditionOperands(result);
                }
            } else if (neighbour != null) {
                AutocompleteUtils.autocompleteConditionOperands(result);
            }
        }
    }
}

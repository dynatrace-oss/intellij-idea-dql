package pl.thedeem.intellij.dpl.completion.providers;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dpl.completion.DPLCompletions;
import pl.thedeem.intellij.dpl.completion.DPLPsiPatterns;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLGroupExpression;

public class DPLConfigurationParameterCompletion {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        if (DPLPsiPatterns.EMPTY_GROUP.accepts(position)) {
            DPLGroupExpression group = PsiTreeUtil.getParentOfType(position, DPLGroupExpression.class);
            if (group != null && group.getParent() instanceof DPLExpressionDefinition currentExpr) {
                DPLExpressionDefinition prevExpression = PsiUtils.getPrevSiblingOfTypeSkippingWhitespaces(currentExpr, DPLExpressionDefinition.class);
                if (prevExpression != null) {
                    showConfigurationOptions(prevExpression, result);
                }
            }
        } else if (DPLPsiPatterns.UNFINISHED_PARAMETERS_LIST.accepts(position)) {
            DPLExpressionDefinition currentExpr = PsiTreeUtil.getParentOfType(position, DPLExpressionDefinition.class);
            if (currentExpr != null) {
                showConfigurationOptions(currentExpr, result);
            }
        }
    }

    private void showConfigurationOptions(@NotNull DPLExpressionDefinition expression, @NotNull CompletionResultSet result) {
        for (Configuration parameter : expression.getAvailableConfiguration()) {
            result.addElement(DPLCompletions.createConfigurationParameterLookup(parameter));
        }
    }
}

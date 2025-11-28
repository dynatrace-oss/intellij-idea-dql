package pl.thedeem.intellij.dpl.completion.providers;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dpl.completion.DPLCompletions;
import pl.thedeem.intellij.dpl.completion.DPLPsiPatterns;
import pl.thedeem.intellij.dpl.definition.model.Configuration;
import pl.thedeem.intellij.dpl.psi.DPLConfiguration;
import pl.thedeem.intellij.dpl.psi.DPLDpl;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLGroupExpression;

import java.util.Map;
import java.util.Set;

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
            DPLDpl block = PsiTreeUtil.getPrevSiblingOfType(position, DPLDpl.class);
            DPLConfiguration configuration = PsiUtils.findDeepLastChildOfType(block, DPLConfiguration.class);
            if (configuration != null && configuration.getParent() instanceof DPLExpressionDefinition currentExpr) {
                showConfigurationOptions(currentExpr, result);
            }
        } else if (DPLPsiPatterns.FINISHED_PARAMETERS_LIST.accepts(position)) {
            DPLExpressionDefinition currentExpr = PsiTreeUtil.getParentOfType(position, DPLExpressionDefinition.class);
            if (currentExpr != null) {
                showConfigurationOptions(currentExpr, result);
            }
        }
    }

    private void showConfigurationOptions(@Nullable DPLExpressionDefinition expression, @NotNull CompletionResultSet result) {
        if (expression == null) {
            return;
        }

        Set<String> definedParameters = expression.getDefinedParameters();
        Map<String, Configuration> allParameters = expression.getConfigurationDefinition();
        if (allParameters == null) {
            return;
        }
        for (Configuration parameter : allParameters.values()) {
            if (!definedParameters.contains(parameter.name().toLowerCase())) {
                result.addElement(DPLCompletions.createConfigurationParameterLookup(parameter));
            }
        }
    }
}

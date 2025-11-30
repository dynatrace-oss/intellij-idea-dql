package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractReplaceElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.*;

public class ReorderElementDefinitionQuickFix extends AbstractReplaceElementQuickFix<DPLExpressionDefinition> {
    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.replace.reorderExpression");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }

    @Override
    protected @Nullable DPLExpressionDefinition getElementToReplace(@NotNull PsiElement element) {
        if (element instanceof DPLExpressionDefinition) {
            return (DPLExpressionDefinition) element;
        }
        return PsiTreeUtil.getParentOfType(element, DPLExpressionDefinition.class);
    }

    @Override
    protected @NotNull String getDefaultReplacement(@NotNull DPLExpressionDefinition expression) {
        String result = "";
        DPLLookaroundExpression lookaround = expression.getLookaround();
        DPLDefinitionExpression definedExpression = expression.getDefinedExpression();
        DPLMatchersExpression matchers = expression.getMatchers();
        DPLConfigurationExpression configuration = expression.getConfiguration();
        DPLQuantifierExpression quantifier = expression.getQuantifier();
        DPLNullableExpression nullable = expression.getNullable();
        DPLFieldName memberName = expression.getMemberName();
        DPLFieldName exportedName = expression.getExportedName();

        if (lookaround != null) {
            result += lookaround.getLookaround().getText();
        }
        if (definedExpression != null) {
            result += definedExpression.getText();
        }
        if (matchers != null && matchers.getCommandMatchersContent() != null) {
            result += "{" + matchers.getCommandMatchersContent().getText() + "}";
        }
        if (configuration != null) {
            result += "(" + configuration.getConfigurationContent().getText() + ")";
        }
        if (quantifier != null) {
            result += quantifier.getQuantifierContent().getText();
        }
        if (nullable != null) {
            result += nullable.getNullable().getText();
        }
        if (memberName != null) {
            result += ":" + memberName.getText();
        }
        if (exportedName != null) {
            result += ":" + exportedName.getText();
        }

        return result;
    }
}

package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractReplaceElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLUnaryExpression;

public class SimplifyNestedNegationQuickFix extends AbstractReplaceElementQuickFix<DQLUnaryExpression> {
    @Override
    protected @Nullable DQLUnaryExpression getElementToReplace(@NotNull PsiElement element) {
        return element instanceof DQLUnaryExpression expression ? expression : PsiTreeUtil.getParentOfType(element, DQLUnaryExpression.class);
    }

    @Override
    protected @NotNull String getDefaultReplacement(@NotNull DQLUnaryExpression element) {
        int amount = 1;
        DQLUnaryExpression bottom = element;
        while (bottom.getExpression() != null && DQLUtil.unpackParenthesis(bottom.getExpression()) instanceof DQLUnaryExpression nested) {
            bottom = nested;
            amount++;
        }
        DQLExpression result = bottom.getExpression();
        if (result == null) {
            result = bottom;
        }
        String negatives = amount % 2 == 1 ? "not " : "";
        return negatives + result.getText();
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.nestedNegation.simplifyFix");
    }

    @Override
    protected boolean useTemplateVariable() {
        return false;
    }
}

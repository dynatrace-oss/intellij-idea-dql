package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractReplaceElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLNegativeValueExpression;

public class SimplifyNegativeValueQuickFix extends AbstractReplaceElementQuickFix<DQLNegativeValueExpression> {
    @Override
    protected @Nullable DQLNegativeValueExpression getElementToReplace(@NotNull PsiElement element) {
        return element instanceof DQLNegativeValueExpression expression ? expression : null;
    }

    @Override
    protected @NotNull String getDefaultReplacement(@NotNull DQLNegativeValueExpression element) {
        int amount = 1;
        DQLNegativeValueExpression bottom = element;
        while (bottom.getExpression() instanceof DQLNegativeValueExpression nested) {
            bottom = nested;
            amount++;
        }
        DQLExpression result = bottom.getExpression();
        if (result == null) {
            result = bottom;
        }
        String negatives = amount % 2 == 1 ? "-" : "";
        return negatives + result.getText();
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.simplifyNegativeValue");
    }

    @Override
    protected boolean useTemplateVariable() {
        return false;
    }
}

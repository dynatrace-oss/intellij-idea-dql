package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractDropElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;

public class DropFunctionQuickFix extends AbstractDropElementQuickFix {
    public DropFunctionQuickFix() {
        super();
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.dropFunction");
    }

    @Override
    protected @NotNull PsiElement getElementToRemove(@NotNull PsiElement original) {
        if (original instanceof DQLFunctionExpression function) {
            return function;
        }
        DQLFunctionExpression function = PsiTreeUtil.getParentOfType(original, DQLFunctionExpression.class);
        if (function != null) {
            return function;
        }
        return super.getElementToRemove(original);
    }
}

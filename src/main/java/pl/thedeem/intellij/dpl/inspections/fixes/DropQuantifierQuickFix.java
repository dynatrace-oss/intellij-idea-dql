package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractDropElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLQuantifierExpression;

public class DropQuantifierQuickFix extends AbstractDropElementQuickFix {
    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.drop.quantifier");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }

    @Override
    protected @NotNull PsiElement getElementToRemove(@NotNull PsiElement original) {
        return original instanceof DPLQuantifierExpression quantifier ? quantifier.getQuantifierContent() : original;
    }
}

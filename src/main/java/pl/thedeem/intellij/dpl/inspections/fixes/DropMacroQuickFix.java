package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractDropElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLMacroDefinitionExpression;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

public class DropMacroQuickFix extends AbstractDropElementQuickFix {
    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.drop.macro");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }

    @Override
    protected @NotNull PsiElement getElementToRemove(@NotNull PsiElement original) {
        if (original instanceof DPLVariable variable && variable.getParent() instanceof DPLMacroDefinitionExpression macro) {
            return macro;
        }
        return super.getElementToRemove(original);
    }
}

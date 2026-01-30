package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractDropElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLCommand;

public class DropCommandQuickFix extends AbstractDropElementQuickFix {
    public DropCommandQuickFix() {
        super();
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.dropCommand");
    }

    @Override
    protected @NotNull PsiElement getElementToRemove(@NotNull PsiElement original) {
        if (original instanceof DQLCommand command) {
            return command;
        }
        DQLCommand command = PsiTreeUtil.getParentOfType(original, DQLCommand.class);
        if (command != null) {
            return command;
        }
        return super.getElementToRemove(original);
    }
}

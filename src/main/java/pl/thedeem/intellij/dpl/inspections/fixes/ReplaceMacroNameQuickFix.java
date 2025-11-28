package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractReplaceElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLMacroDefinitionExpression;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

import java.util.Objects;

public class ReplaceMacroNameQuickFix extends AbstractReplaceElementQuickFix<DPLVariable> {
    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.replace.macroName");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }

    @Override
    protected @Nullable DPLVariable getElementToReplace(@NotNull PsiElement element) {
        if (element instanceof DPLMacroDefinitionExpression macro) {
            return macro.getVariable();
        }
        if (element instanceof DPLVariable variable) {
            return variable;
        }
        return null;
    }

    @Override
    protected @NotNull String getDefaultReplacement(@NotNull DPLVariable element) {
        return Objects.requireNonNullElse(element.getName(), "");
    }
}

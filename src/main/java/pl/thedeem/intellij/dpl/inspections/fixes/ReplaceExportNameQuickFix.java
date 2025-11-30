package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractReplaceElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;

import java.util.Objects;

public class ReplaceExportNameQuickFix extends AbstractReplaceElementQuickFix<DPLFieldName> {
    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.replace.exportName");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }

    @Override
    protected @Nullable DPLFieldName getElementToReplace(@NotNull PsiElement element) {
        if (element instanceof DPLFieldName fieldName) {
            return fieldName;
        }
        return null;
    }

    @Override
    protected @NotNull String getDefaultReplacement(@NotNull DPLFieldName element) {
        return Objects.requireNonNullElse(element.getName(), "");
    }
}

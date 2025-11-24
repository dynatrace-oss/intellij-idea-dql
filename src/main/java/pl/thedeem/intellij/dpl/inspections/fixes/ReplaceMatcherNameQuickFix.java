package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractReplaceElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLMatcher;
import pl.thedeem.intellij.dpl.psi.DPLMatcherName;

import java.util.Objects;

public class ReplaceMatcherNameQuickFix extends AbstractReplaceElementQuickFix<DPLMatcherName> {
    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.replace.kvpParameterName");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }

    @Override
    protected @Nullable DPLMatcherName getElementToReplace(@NotNull PsiElement element) {
        if (element instanceof DPLMatcher matcher) {
            return matcher.getMatcherName();
        }
        return null;
    }

    @Override
    protected @NotNull String getDefaultReplacement(@NotNull DPLMatcherName element) {
        return Objects.requireNonNullElse(element.getName(), "");
    }
}

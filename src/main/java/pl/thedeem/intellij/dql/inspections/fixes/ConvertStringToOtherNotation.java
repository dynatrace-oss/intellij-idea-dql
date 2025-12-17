package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractReplaceElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLString;

public class ConvertStringToOtherNotation extends AbstractReplaceElementQuickFix<DQLString> {
    private final String quotes;

    public ConvertStringToOtherNotation(@NotNull String quotes) {
        this.quotes = quotes;
    }

    @Override
    protected @Nullable DQLString getElementToReplace(@NotNull PsiElement element) {
        return element instanceof DQLString string ? string : null;
    }

    @Override
    protected @NotNull String getDefaultReplacement(@NotNull DQLString element) {
        return quotes + element.getContent() + quotes;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.singleQuotes.fix.convertToDouble");
    }

    @Override
    protected boolean useTemplateVariable() {
        return false;
    }
}

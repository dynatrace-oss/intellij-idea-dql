package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractDropListedElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLTypes;

public class DropKvpMatcherEntryQuickFix extends AbstractDropListedElementQuickFix {
    public DropKvpMatcherEntryQuickFix() {
        super(DPLTypes.COMMA);
    }

    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.drop.kvpParameterName");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }
}

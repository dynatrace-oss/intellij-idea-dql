package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractDropElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;

public class DropGroupQuickFix extends AbstractDropElementQuickFix {
    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.drop.groupExpression");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }
}

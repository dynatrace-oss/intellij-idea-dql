package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractDropListedElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLTypes;

import java.util.List;

public class DropElementQuickFix extends AbstractDropListedElementQuickFix {
    public DropElementQuickFix() {
        super(List.of(DQLTypes.COMMA));
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.dropElement");
    }
}

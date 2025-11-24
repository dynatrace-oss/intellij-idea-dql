package pl.thedeem.intellij.dpl.indexing;

import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

public class DPLUsageTypeProvider implements UsageTypeProvider {
    @Override
    public @Nullable UsageType getUsageType(@NotNull PsiElement psiElement) {
        if (psiElement instanceof DPLFieldName) {
            return UsageType.WRITE;
        }
        if (psiElement instanceof DPLVariable variable) {
            if (variable.isDefinition()) {
                return UsageType.WRITE;
            }
            return UsageType.READ;
        }
        return null;
    }
}

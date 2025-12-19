package pl.thedeem.intellij.dql.indexing;

import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.*;

public class DQLUsageTypeProvider implements UsageTypeProvider {
    @Override
    public @Nullable UsageType getUsageType(@NotNull PsiElement psiElement) {
        if (
                psiElement instanceof DQLVariableExpression
                        || psiElement instanceof DQLFunctionName
                        || psiElement instanceof DQLCommandKeyword
        ) {
            return UsageType.READ;
        }
        if (psiElement instanceof DQLFieldExpression) {
            if (psiElement.getParent() instanceof DQLAssignExpression) {
                return UsageType.WRITE;
            }
            return UsageType.READ;
        }
        return null;
    }
}

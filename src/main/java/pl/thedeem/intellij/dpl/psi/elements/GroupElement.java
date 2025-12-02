package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.psi.DPLParameterExpression;

import java.util.List;

public interface GroupElement extends PsiElement, BaseExpression {
    boolean isPotentiallyConfiguration(@Nullable PsiElement childToSkip);

    @NotNull List<DPLParameterExpression> getParameters();
}

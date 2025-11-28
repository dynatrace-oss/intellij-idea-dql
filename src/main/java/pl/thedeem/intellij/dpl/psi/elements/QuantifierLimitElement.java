package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

public interface QuantifierLimitElement extends PsiNamedElement {
    @NotNull Long getLongValue();
}

package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public interface QuantifierElement extends PsiElement, ModifierExpression {
    @NotNull MinMaxValues getMinMaxValues();

    record MinMaxValues(Long min, Long max) {
    }
}

package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface TimeAlignmentExpression extends BaseElement, TwoSidesExpression {
    @Nullable PsiElement getDurationElement();
}

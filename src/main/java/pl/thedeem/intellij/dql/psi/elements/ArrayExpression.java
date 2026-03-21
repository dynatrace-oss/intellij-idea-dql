package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface ArrayExpression extends BaseElement, OperatorElement {
    @Nullable PsiElement getBaseExpression();
}

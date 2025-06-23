package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public interface BaseNamedElement extends PsiNamedElement, BaseElement {
    @Override
    default PsiElement setName(@NotNull String var1) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }
}

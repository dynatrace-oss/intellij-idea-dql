package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface VariableElement extends BaseNameOwnerElement {
    @Nullable PsiElement getDefinition();
    @Nullable String getValue();
}

package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.services.definition.model.Command;

public interface CommandElement extends BaseNamedElement, DQLParametersOwner {
    @Nullable Command getDefinition();

    @Nullable PsiElement getPipe();

    boolean isFirstStatement();
}

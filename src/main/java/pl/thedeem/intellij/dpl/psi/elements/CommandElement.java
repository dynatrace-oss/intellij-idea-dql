package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;

public interface CommandElement extends PsiNamedElement, BaseExpression {
    @Nullable ExpressionDescription getDefinition();
}

package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.definition.model.Command;

public interface CommandElement extends PsiNamedElement {
    @Nullable Command getDefinition();
}

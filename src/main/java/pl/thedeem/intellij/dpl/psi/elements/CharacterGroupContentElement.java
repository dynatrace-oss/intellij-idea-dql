package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;

public interface CharacterGroupContentElement extends PsiNamedElement {
    @NotNull String getRegex();
}

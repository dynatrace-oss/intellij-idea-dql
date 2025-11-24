package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.psi.DPLMacroDefinitionExpression;

public interface VariableElement extends PsiNameIdentifierOwner {
    boolean isDefinition();
    @Nullable DPLMacroDefinitionExpression getDefinition();
}

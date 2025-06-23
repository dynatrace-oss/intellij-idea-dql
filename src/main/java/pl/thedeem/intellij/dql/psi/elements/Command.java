package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.DQLCommandDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Command extends BaseNamedElement, DQLParametersOwner {
    @NotNull List<DQLParameterDefinition> getMissingExclusiveRequiredParameters();
    @Nullable DQLCommandDefinition getDefinition();
    @Nullable PsiElement getPipe();
    boolean isFirstStatement();
}

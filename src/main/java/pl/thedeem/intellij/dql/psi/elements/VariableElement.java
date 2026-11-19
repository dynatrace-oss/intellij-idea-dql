package pl.thedeem.intellij.dql.psi.elements;

import org.jetbrains.annotations.Nullable;

public interface VariableElement extends BaseNameOwnerElement {
    @Nullable String getValue();
}

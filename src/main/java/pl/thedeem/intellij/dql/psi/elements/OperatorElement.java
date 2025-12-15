package pl.thedeem.intellij.dql.psi.elements;

import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.model.Operator;
import pl.thedeem.intellij.dql.definition.model.Signature;

public interface OperatorElement {
    @Nullable Operator getDefinition();

    @Nullable Signature getSignature();
}

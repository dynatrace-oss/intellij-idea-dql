package pl.thedeem.intellij.dql.psi.elements;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Signature;
import pl.thedeem.intellij.dql.psi.DQLExpression;

import java.util.List;

public interface FunctionCallExpression extends BaseNamedElement, DQLParametersOwner {
    @Nullable Function getDefinition();

    @NotNull List<DQLExpression> getFunctionArguments();

    @Nullable Signature getSignature();
}

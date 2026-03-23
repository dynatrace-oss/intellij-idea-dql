package pl.thedeem.intellij.dql.psi.elements;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.services.definition.model.Function;
import pl.thedeem.intellij.dql.services.definition.model.Signature;

import java.util.List;

public interface FunctionCallExpression extends BaseNamedElement, DQLParametersOwner {
    @Nullable Function getDefinition();

    @NotNull List<DQLExpression> getFunctionArguments();

    @Nullable Signature getSignature();
}

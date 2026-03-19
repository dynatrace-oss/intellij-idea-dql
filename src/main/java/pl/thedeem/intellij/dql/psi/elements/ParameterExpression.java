package pl.thedeem.intellij.dql.psi.elements;

import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.model.Parameter;

public interface ParameterExpression extends BaseNamedElement {
    @Nullable Parameter definition();
}

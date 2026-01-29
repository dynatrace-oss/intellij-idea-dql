package pl.thedeem.intellij.dql.psi.elements;

import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLQuery;

public interface FieldElement extends BaseNameOwnerElement {
    @Nullable DQLAssignExpression getAssignExpression();

    @Nullable DQLQuery getParentQuery();
}

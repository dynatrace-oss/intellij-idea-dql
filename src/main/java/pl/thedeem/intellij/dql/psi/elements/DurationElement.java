package pl.thedeem.intellij.dql.psi.elements;

import pl.thedeem.intellij.dql.definition.model.DQLDurationType;

public interface DurationElement extends BaseNamedElement {
    Number getNumberPart();

    DQLDurationType getDurationType();
}

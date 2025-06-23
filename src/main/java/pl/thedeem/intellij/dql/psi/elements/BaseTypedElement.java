package pl.thedeem.intellij.dql.psi.elements;

import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.Set;

public interface BaseTypedElement {
    default Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.ANY);
    }
    default boolean accessesData() {
        return true;
    }
    String getFieldName();
}

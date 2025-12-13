package pl.thedeem.intellij.dql.psi.elements;


import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

public interface BaseTypedElement {
    default @NotNull Collection<String> getDataType() {
        return Set.of();
    }

    default boolean accessesData() {
        return true;
    }

    String getFieldName();
}

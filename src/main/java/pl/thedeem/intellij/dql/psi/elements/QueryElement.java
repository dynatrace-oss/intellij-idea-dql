package pl.thedeem.intellij.dql.psi.elements;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface QueryElement extends BaseElement {
    @NotNull Map<String, List<VariableElement>> getDefinedVariables();
}

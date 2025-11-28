package pl.thedeem.intellij.dql.psi.elements;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.psi.DQLExpression;

import java.util.List;
import java.util.Set;

public interface DQLParametersOwner {
    @NotNull List<DQLParameterObject> getParameters();
    @Nullable DQLParameterObject findParameter(@NotNull String name);
    @NotNull Set<DQLParameterDefinition> getDefinedParameters();
    @NotNull Set<String> getDefinedParameterNames();
    @NotNull List<DQLParameterDefinition> getMissingRequiredParameters();
    @Nullable DQLParameterObject getParameter(@NotNull DQLExpression parameter);
}

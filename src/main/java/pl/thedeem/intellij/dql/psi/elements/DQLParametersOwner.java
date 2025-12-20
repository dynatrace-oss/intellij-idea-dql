package pl.thedeem.intellij.dql.psi.elements;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.util.Collection;
import java.util.List;

public interface DQLParametersOwner {
    @NotNull List<MappedParameter> getParameters();

    @Nullable MappedParameter findParameter(@NotNull String name);

    @NotNull Collection<Parameter> getMissingRequiredParameters();

    @NotNull Collection<Parameter> getMissingParameters();

    @Nullable MappedParameter getParameter(@NotNull PsiElement parameter);
}

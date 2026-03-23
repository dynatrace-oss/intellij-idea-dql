package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.List;

public interface ParameterValidator {
    @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition);
}

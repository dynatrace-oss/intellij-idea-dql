package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConstantValueValidator extends AbstractDefinitionValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        if (!(parameter instanceof BaseElement element) || !element.accessesData()) {
            return List.of();
        }
        return List.of(new DQLParameterValueTypesValidator.ValueIssue(
                parameter,
                DQLBundle.message(
                        "inspection.parameter.constantValue.invalidValue",
                        definition.name(),
                        DQLBundle.types(Objects.requireNonNullElse(definition.valueTypes(), Set.of()), parameter.getProject())
                )
        ));
    }
}

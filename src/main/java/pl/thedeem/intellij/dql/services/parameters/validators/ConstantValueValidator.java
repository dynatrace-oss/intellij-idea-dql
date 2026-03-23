package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConstantValueValidator extends AbstractParameterValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        if (!definition.parameterValueTypes().contains("dql.parameterValueType.expressionWithConstantValue")) {
            return List.of();
        }
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

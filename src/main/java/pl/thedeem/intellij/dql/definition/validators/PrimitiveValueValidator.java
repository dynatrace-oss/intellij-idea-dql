package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLSimpleExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrimitiveValueValidator extends AbstractDefinitionValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        for (PsiElement invalidValue : getInvalidValues(parameter, element -> isInvalid(element, definition))) {
            result.add(new DQLParameterValueTypesValidator.ValueIssue(
                    invalidValue,
                    DQLBundle.message(
                            "inspection.parameter.primitiveValue.invalidValue",
                            definition.name(),
                            DQLBundle.types(definition.valueTypes(), invalidValue.getProject())
                    )
            ));
        }
        return result;
    }

    private boolean isInvalid(@NotNull PsiElement element, @NotNull Parameter definition) {
        if (element instanceof DQLSimpleExpression) {
            return false;
        }
        List<String> types = Objects.requireNonNullElse(definition.parameterValueTypes(), List.of());
        return !(element instanceof DQLFieldExpression) || types.stream().noneMatch(DQLDefinitionService.FIELD_IDENTIFIER_PARAMETER_VALUE_TYPES::contains);
    }
}

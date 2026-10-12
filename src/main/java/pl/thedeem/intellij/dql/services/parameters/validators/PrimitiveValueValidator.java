package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLPrimitiveExpression;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.List;

public class PrimitiveValueValidator extends AbstractParameterValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        if (!definition.parameterValueTypes().contains("dql.parameterValueType.primitiveValue")) {
            return List.of();
        }
        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        DQLDefinitionService service = DQLDefinitionService.getInstance(parameter.getProject());
        for (PsiElement invalidValue : getInvalidValues(parameter, element -> isInvalid(service, element, definition))) {
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

    private boolean isInvalid(@NotNull DQLDefinitionService service, @NotNull PsiElement element, @NotNull Parameter definition) {
        if (element instanceof DQLPrimitiveExpression) {
            return false;
        }
        return !(element instanceof DQLFieldExpression) || definition.parameterValueTypes()
                .stream()
                .noneMatch(service.getFieldIdentifierParameterValueTypes()::contains);
    }
}

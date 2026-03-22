package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLPrimitiveExpression;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.List;

public class EnumValuesValidator extends AbstractParameterValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        if (!definition.parameterValueTypes().contains("dql.parameterValueType.enum")) {
            return List.of();
        }

        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        if (definition.allowedEnumValues() == null) {
            return result;
        }
        for (PsiElement invalidValue : getInvalidValues(parameter, e -> isInvalid(e, definition))) {
            result.add(new DQLParameterValueTypesValidator.ValueIssue(
                    invalidValue,
                    DQLBundle.message(
                            "inspection.parameter.enumValue.invalidValue",
                            definition.name(),
                            DQLBundle.print(definition.allowedEnumValues())
                    )
            ));
        }
        return result;
    }

    private boolean isInvalid(PsiElement e, @NotNull Parameter definition) {
        if (!(e instanceof DQLPrimitiveExpression) && !(e instanceof DQLFieldExpression)) {
            return false;
        }
        return !definition.allowedEnumValues().contains(e.getText().trim());
    }
}

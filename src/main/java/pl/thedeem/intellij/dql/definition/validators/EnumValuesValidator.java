package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLPrimitiveExpression;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.List;

public class EnumValuesValidator extends AbstractDefinitionValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        if (definition.allowedEnumValues() == null) {
            return result;
        }
        for (PsiElement invalidValue : getInvalidValues(parameter, e -> e instanceof DQLPrimitiveExpression && !definition.allowedEnumValues().contains(e.getText().trim()))) {
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
}

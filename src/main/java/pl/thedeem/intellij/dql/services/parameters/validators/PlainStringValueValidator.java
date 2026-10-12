package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLString;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.List;

public class PlainStringValueValidator extends AbstractParameterValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        DQLDefinitionService service = DQLDefinitionService.getInstance(parameter.getProject());
        if (definition.parameterValueTypes().stream().noneMatch(service.getStringParameterValueTypes()::contains)) {
            return List.of();
        }

        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        for (PsiElement invalidValue : getInvalidValues(parameter, element -> !(element instanceof DQLString) && !(element instanceof DQLVariableExpression))) {
            result.add(new DQLParameterValueTypesValidator.ValueIssue(
                            invalidValue,
                            DQLBundle.message(
                                    "inspection.parameter.plainString.invalidValue",
                                    definition.name())
                    )
            );
        }
        return result;
    }
}

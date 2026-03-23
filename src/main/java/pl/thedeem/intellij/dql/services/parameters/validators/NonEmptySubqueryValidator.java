package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLSubqueryExpression;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.List;

public class NonEmptySubqueryValidator extends SubqueryValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        if (!definition.parameterValueTypes().contains("dql.parameterValueType.nonEmptyExecutionBlock")) {
            return List.of();
        }

        List<DQLParameterValueTypesValidator.ValueIssue> found = super.validate(parameter, definition);
        if (!found.isEmpty()) {
            return found;
        }

        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        for (PsiElement invalidValue : getInvalidValues(parameter, this::isInvalid)) {
            result.add(new DQLParameterValueTypesValidator.ValueIssue(
                    invalidValue,
                    DQLBundle.message("inspection.parameter.executionBlock.invalidValue", definition.name())
            ));
        }
        return result;
    }

    private boolean isInvalid(PsiElement element) {
        return !(element instanceof DQLSubqueryExpression block) || block.getQuery() == null;
    }
}

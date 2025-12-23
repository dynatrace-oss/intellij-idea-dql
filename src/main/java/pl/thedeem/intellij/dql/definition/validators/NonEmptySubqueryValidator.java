package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLSubqueryExpression;

import java.util.ArrayList;
import java.util.List;

public class NonEmptySubqueryValidator extends SubqueryValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<DQLParameterValueTypesValidator.ValueIssue> found = super.validate(parameter, definition);
        if (!found.isEmpty()) {
            return found;
        }

        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        for (PsiElement invalidValue : getInvalidValues(parameter, e -> !(e instanceof DQLSubqueryExpression block) || block.getQuery() == null)) {
            result.add(new DQLParameterValueTypesValidator.ValueIssue(
                    invalidValue,
                    DQLBundle.message("inspection.parameter.executionBlock.invalidValue", definition.name())
            ));
        }
        return result;
    }
}

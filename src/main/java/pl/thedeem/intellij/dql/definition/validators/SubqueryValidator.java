package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLSubqueryExpression;

import java.util.ArrayList;
import java.util.List;

public class SubqueryValidator extends AbstractDefinitionValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        for (PsiElement invalidValue : getInvalidValues(parameter, e -> !(e instanceof DQLSubqueryExpression))) {
            result.add(new DQLParameterValueTypesValidator.ValueIssue(
                    invalidValue,
                    DQLBundle.message("inspection.parameter.executionBlock.required", definition.name())
            ));
        }
        return result;
    }
}

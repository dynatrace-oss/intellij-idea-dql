package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLArrayExpression;
import pl.thedeem.intellij.dql.psi.DQLEqualityExpression;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.List;

public class JoinConditionValidator extends AbstractParameterValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        if (!definition.parameterValueTypes().contains("dql.parameterValueType.joinCondition")) {
            return List.of();
        }

        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        for (PsiElement invalidValue : getInvalidValues(parameter, this::isElementInvalid)) {
            result.add(new DQLParameterValueTypesValidator.ValueIssue(
                    invalidValue,
                    DQLBundle.message(
                            "inspection.parameter.joinCondition.invalidCondition",
                            definition.name()
                    )
            ));
        }
        return result;
    }

    private static boolean isJoinOperandInvalid(PsiElement expression, String expectedName) {
        if (expression instanceof DQLArrayExpression arrayExpression) {
            if (arrayExpression.getBaseExpression() instanceof DQLFieldExpression fieldExpression) {
                return !StringUtil.equalsIgnoreCase(expectedName, fieldExpression.getName());
            }
        }
        return true;
    }

    private boolean isElementInvalid(PsiElement element) {
        if (element instanceof DQLEqualityExpression expr) {
            return isJoinOperandInvalid(expr.getLeftExpression(), "left")
                    || isJoinOperandInvalid(expr.getRightExpression(), "right");
        }
        return !(element instanceof DQLFieldExpression);
    }
}

package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLArrayExpression;
import pl.thedeem.intellij.dql.psi.DQLEqualityExpression;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JoinConditionValidator extends AbstractDefinitionValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        for (PsiElement invalidValue : getInvalidValues(parameter, e -> {
            if (e instanceof DQLEqualityExpression expr) {
                return isJoinOperandInvalid(expr.getLeftExpression(), Set.of("left"))
                        || isJoinOperandInvalid(expr.getRightExpression(), Set.of("right"));
            } else return !(e instanceof DQLFieldExpression);
        })) {
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

    private static boolean isJoinOperandInvalid(PsiElement expression, Set<String> fieldNames) {
        if (expression instanceof DQLArrayExpression arrayExpression) {
            if (arrayExpression.getLeftExpression() instanceof DQLFieldExpression fieldExpression) {
                return !fieldNames.contains(fieldExpression.getName());
            }
        }
        return true;
    }
}

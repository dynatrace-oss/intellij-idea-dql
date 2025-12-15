package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLArrayExpression;
import pl.thedeem.intellij.dql.psi.DQLEqualityExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;

import java.util.List;
import java.util.Set;

public class JoinConditionParameterInspection extends AbstractParameterValueTypeInspection {
    @Override
    protected void validateParameterValueType(@NotNull DQLExpression expression, @NotNull MappedParameter parameter, @NotNull Parameter definition, @NotNull List<String> parameterTypes, @NotNull ProblemsHolder holder) {
        if (!parameterTypes.contains("dql.parameterValueType.joinCondition")) {
            return;
        }
        for (PsiElement invalidValue : getInvalidValues(expression, e -> {
            if (e instanceof DQLEqualityExpression expr) {
                return isJoinOperandInvalid(expr.getLeftExpression(), Set.of("left"))
                        || isJoinOperandInvalid(expr.getRightExpression(), Set.of("right"));
            } else return !(e instanceof DQLFieldExpression);
        })) {
            holder.registerProblem(
                    invalidValue,
                    DQLBundle.message("inspection.parameter.joinCondition.invalidCondition", definition.name())
            );
        }
    }

    private static boolean isJoinOperandInvalid(DQLExpression expression, Set<String> fieldNames) {
        if (expression instanceof DQLArrayExpression arrayExpression) {
            if (arrayExpression.getLeftExpression() instanceof DQLFieldExpression fieldExpression) {
                return !fieldNames.contains(fieldExpression.getName());
            }
        }

        return true;
    }
}

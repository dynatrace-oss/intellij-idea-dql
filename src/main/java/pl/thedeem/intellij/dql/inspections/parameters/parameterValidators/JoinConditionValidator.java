package pl.thedeem.intellij.dql.inspections.parameters.parameterValidators;

import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.psi.DQLArrayExpression;
import pl.thedeem.intellij.dql.psi.DQLEqualityExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;

import java.util.Set;

public class JoinConditionValidator implements ParameterValidator {

    @Override
    public boolean isElementInvalid(PsiElement element) {
        if (element instanceof DQLEqualityExpression expression) {
            return isJoinOperandInvalid(expression.getLeftExpression(), Set.of("left"))
                    || isJoinOperandInvalid(expression.getRightExpression(), Set.of("right"));
        }
        else return !(element instanceof DQLFieldExpression);
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

package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.List;
import java.util.Set;

public class InvalidArithmeticalTargetInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLNumber.class,
            DQLDuration.class,
            DQLFieldExpression.class,
            DQLVariableExpression.class,
            DQLArithmeticalExpression.class,
            DQLFunctionCallExpression.class,
            DQLArrayExpression.class,
            DQLTimeAlignmentNowExpression.class,
            DQLTimeAlignmentAtExpression.class,
            DQLParenthesisedExpression.class,
            DQLNegativeValueExpression.class
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitArithmeticalExpression(@NotNull DQLArithmeticalExpression expression) {
                super.visitArithmeticalExpression(expression);
                validateArithmeticalTarget(holder, expression.getExpressionList());
            }
        };
    }

    private void validateArithmeticalTarget(@NotNull ProblemsHolder holder, List<DQLExpression> expressionList) {
        for (DQLExpression operand : expressionList) {
            validateExpressionOperands(holder, operand, ALLOWED_EXPRESSIONS);
        }
    }
}

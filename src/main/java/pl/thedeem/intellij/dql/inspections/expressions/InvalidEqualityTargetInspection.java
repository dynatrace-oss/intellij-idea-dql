package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.Set;

public class InvalidEqualityTargetInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLConditionExpression.class,
            DQLArithmeticalExpression.class,
            DQLEqualityExpression.class,
            DQLComparisonExpression.class,
            DQLUnaryExpression.class,
            DQLArrayExpression.class,
            DQLSimpleExpression.class,
            DQLInExpression.class,
            DQLTimeAlignmentNowExpression.class,
            DQLTimeAlignmentAtExpression.class,
            DQLFieldExpression.class,
            DQLVariableExpression.class,
            DQLParenthesisedExpression.class,
            DQLFunctionCallExpression.class,
            DQLNegativeValueExpression.class
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitEqualityExpression(@NotNull DQLEqualityExpression expression) {
                super.visitEqualityExpression(expression);
                for (DQLExpression operand : expression.getExpressionList()) {
                    validateExpressionOperands(holder, operand, ALLOWED_EXPRESSIONS);
                }
            }
        };
    }
}

package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.Set;

public class InvalidConditionTargetInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLBoolean.class,
            DQLConditionExpression.class,
            DQLComparisonExpression.class,
            DQLEqualityExpression.class,
            DQLUnaryExpression.class,
            DQLVariableExpression.class,
            DQLFieldExpression.class,
            DQLFunctionCallExpression.class,
            DQLParenthesisedExpression.class,
            DQLArrayExpression.class,
            DQLInExpression.class,
            DQLSearchExpression.class
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitConditionExpression(@NotNull DQLConditionExpression expression) {
                super.visitConditionExpression(expression);
                for (DQLExpression operand : expression.getExpressionList()) {
                    validateExpressionOperands(holder, operand, ALLOWED_EXPRESSIONS);
                }
            }
        };
    }
}

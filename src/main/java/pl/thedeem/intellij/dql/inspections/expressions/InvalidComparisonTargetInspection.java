package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.Set;

public class InvalidComparisonTargetInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLNumber.class,
            DQLDuration.class,
            DQLFieldExpression.class,
            DQLVariableExpression.class,
            DQLAdditiveExpression.class,
            DQLMultiplicativeExpression.class,
            DQLFunctionCallExpression.class,
            DQLTimeAlignmentNowExpression.class,
            DQLTimeAlignmentAtExpression.class,
            DQLParenthesisedExpression.class,
            DQLArrayExpression.class,
            DQLString.class,
            DQLNegativeValueExpression.class
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitComparisonExpression(@NotNull DQLComparisonExpression expression) {
                super.visitComparisonExpression(expression);
                for (DQLExpression operand : expression.getExpressionList()) {
                    validateExpressionOperands(holder, operand, ALLOWED_EXPRESSIONS);
                }
            }
        };
    }
}

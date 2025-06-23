package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.Set;

public class InvalidAssignTargetInspection extends BaseInspection {
    public static final Set<Class<?>> LEFT_SIDE_ALLOWED_EXPRESSIONS = Set.of(DQLFieldExpression.class);
    public static final Set<Class<?>> RIGHT_SIDE_ALLOWED_EXPRESSIONS = Set.of(
            DQLConditionExpression.class,
            DQLComparisonExpression.class,
            DQLUnaryExpression.class,
            DQLBracketExpression.class,
            DQLAdditiveExpression.class,
            DQLMultiplicativeExpression.class,
            DQLEqualityExpression.class,
            DQLInExpression.class,
            DQLTimeAlignmentNowExpression.class,
            DQLTimeAlignmentAtExpression.class,
            DQLSubqueryExpression.class,
            DQLSimpleExpression.class,
            DQLFieldExpression.class,
            DQLVariableExpression.class,
            DQLArrayExpression.class,
            DQLParenthesisedExpression.class,
            DQLFunctionCallExpression.class,
            DQLNegativeValueExpression.class
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitAssignExpression(@NotNull DQLAssignExpression expression) {
                super.visitAssignExpression(expression);
                validateExpressionOperands(holder, expression.getLeftExpression(), LEFT_SIDE_ALLOWED_EXPRESSIONS);
                validateExpressionOperands(holder, expression.getRightExpression(), RIGHT_SIDE_ALLOWED_EXPRESSIONS);
            }
        };
    }
}

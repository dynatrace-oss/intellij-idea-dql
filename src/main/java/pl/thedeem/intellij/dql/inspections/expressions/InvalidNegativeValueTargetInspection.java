package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.Set;

public class InvalidNegativeValueTargetInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLDuration.class,
            DQLNumber.class,
            DQLAdditiveExpression.class,
            DQLMultiplicativeExpression.class,
            DQLFieldExpression.class,
            DQLVariableExpression.class,
            DQLNegativeValueExpression.class,
            DQLTimeAlignmentAtExpression.class,
            DQLTimeAlignmentNowExpression.class,
            DQLFunctionCallExpression.class,
            DQLArrayExpression.class
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitNegativeValueExpression(@NotNull DQLNegativeValueExpression expression) {
                super.visitNegativeValueExpression(expression);
                validateExpressionOperands(holder, expression.getExpression(), ALLOWED_EXPRESSIONS);
            }
        };
    }
}

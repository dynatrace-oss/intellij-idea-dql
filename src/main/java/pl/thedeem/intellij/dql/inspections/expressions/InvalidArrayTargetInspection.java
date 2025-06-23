package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.Set;

public class InvalidArrayTargetInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLFieldExpression.class,
            DQLVariableExpression.class,
            DQLArrayExpression.class,
            DQLParenthesisedExpression.class,
            DQLFunctionCallExpression.class
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitArrayExpression(@NotNull DQLArrayExpression expression) {
                super.visitArrayExpression(expression);
                validateExpressionOperands(holder, expression.getLeftExpression(), ALLOWED_EXPRESSIONS);
            }
        };
    }
}

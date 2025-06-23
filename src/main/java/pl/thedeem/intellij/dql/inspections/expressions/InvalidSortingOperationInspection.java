package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.Set;

public class InvalidSortingOperationInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLFunctionCallExpression.class,
            DQLFieldExpression.class,
            DQLArrayExpression.class,
            DQLVariableExpression.class
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitSortExpression(@NotNull DQLSortExpression expression) {
                super.visitSortExpression(expression);
                validateExpressionOperands(holder, expression.getExpression(), ALLOWED_EXPRESSIONS);
            }
        };
    }
}

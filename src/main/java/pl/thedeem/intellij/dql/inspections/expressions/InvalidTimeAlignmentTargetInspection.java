package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.*;

import java.util.Set;

public class InvalidTimeAlignmentTargetInspection extends BaseInspection {
    public static final Set<Class<?>> ALLOWED_EXPRESSIONS = Set.of(
            DQLDuration.class,
            DQLString.class,
            DQLParenthesisedExpression.class,
            DQLFunctionCallExpression.class,
            DQLTimeAlignmentNowExpression.class,
            DQLTimeAlignmentAtExpression.class,
            DQLFieldExpression.class,
            DQLArrayExpression.class
    );

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitTimeAlignmentAtExpression(@NotNull DQLTimeAlignmentAtExpression expression) {
                super.visitTimeAlignmentAtExpression(expression);
                validateExpressionOperands(holder, expression.getExpression(), ALLOWED_EXPRESSIONS);
            }
        };
    }
}
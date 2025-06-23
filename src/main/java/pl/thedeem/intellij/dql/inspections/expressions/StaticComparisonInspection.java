package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.DQLComparisonExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class StaticComparisonInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitComparisonExpression(@NotNull DQLComparisonExpression expression) {
                super.visitComparisonExpression(expression);

                if (hasOnlyStaticValues(expression.getExpressionList())) {
                    holder.registerProblem(expression, DQLBundle.message("inspection.expression.operand.onlyStatic"));
                }
            }
        };
    }
}

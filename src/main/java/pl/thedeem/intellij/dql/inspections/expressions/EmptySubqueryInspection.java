package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.DQLSubqueryExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class EmptySubqueryInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitSubqueryExpression(@NotNull DQLSubqueryExpression expression) {
                super.visitSubqueryExpression(expression);
                if (expression.getQuery() == null) {
                    holder.registerProblem(expression, DQLBundle.message("inspection.subquery.body.empty"));
                }
            }
        };
    }
}

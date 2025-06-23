package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.DQLConditionExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class StaticConditionInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitConditionExpression(@NotNull DQLConditionExpression expression) {
                super.visitConditionExpression(expression);

                if (hasOnlyStaticValues(expression.getExpressionList())) {
                    holder.registerProblem(expression, DQLBundle.message("inspection.expression.operand.onlyStatic"));
                }
            }
        };
    }
}

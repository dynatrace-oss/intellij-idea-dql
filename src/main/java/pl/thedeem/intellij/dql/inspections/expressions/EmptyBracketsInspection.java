package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class EmptyBracketsInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitBracketExpression(@NotNull DQLBracketExpression expression) {
                super.visitBracketExpression(expression);
                if (expression.getExpressionList().isEmpty()) {
                    holder.registerProblem(
                            expression,
                            DQLBundle.message("inspection.bracket.body.empty")
                    );
                }
            }
        };
    }
}

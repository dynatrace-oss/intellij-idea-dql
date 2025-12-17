package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.inspections.fixes.SimplifyNestedNegationQuickFix;
import pl.thedeem.intellij.dql.psi.DQLParenthesisedExpression;
import pl.thedeem.intellij.dql.psi.DQLUnaryExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class NestedNegationInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitUnaryExpression(@NotNull DQLUnaryExpression expression) {
                super.visitUnaryExpression(expression);

                if (expression.getExpression() == null || !(DQLUtil.unpackParenthesis(expression.getExpression()) instanceof DQLUnaryExpression)) {
                    return;
                }

                DQLUnaryExpression topUnary = findTopUnary(expression);
                if (topUnary == expression) {
                    holder.registerProblem(
                            topUnary.getUnaryOperator(),
                            DQLBundle.message("inspection.nestedNegation.canBeSimplified"),
                            new SimplifyNestedNegationQuickFix()
                    );
                }
            }
        };
    }

    private @NotNull DQLUnaryExpression findTopUnary(@NotNull DQLUnaryExpression expression) {
        DQLUnaryExpression result = expression;
        PsiElement element = expression;

        while (element.getParent() instanceof DQLUnaryExpression || element.getParent() instanceof DQLParenthesisedExpression) {
            element = element.getParent();
            if (element instanceof DQLUnaryExpression unary) {
                result = unary;
            }
        }
        return result;
    }
}

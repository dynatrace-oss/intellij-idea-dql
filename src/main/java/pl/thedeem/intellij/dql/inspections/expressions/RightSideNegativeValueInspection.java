package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.fixes.ParenthesizeElement;
import pl.thedeem.intellij.dql.psi.*;

import java.util.List;

public class RightSideNegativeValueInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitMultiplicativeExpression(@NotNull DQLMultiplicativeExpression expression) {
                super.visitMultiplicativeExpression(expression);
                List<DQLExpression> expressions = expression.getExpressionList();
                handleExpressions(expressions.getFirst(), expressions.getLast(), holder);
            }

            @Override
            public void visitAdditiveExpression(@NotNull DQLAdditiveExpression expression) {
                super.visitAdditiveExpression(expression);
                List<DQLExpression> expressions = expression.getExpressionList();
                handleExpressions(expressions.getFirst(), expressions.getLast(), holder);
            }
        };
    }

    private void handleExpressions(DQLExpression leftSide, DQLExpression rightSide, @NotNull ProblemsHolder holder) {
        if (leftSide instanceof DQLNegativeValueExpression) {
            PsiElement parent = leftSide.getParent();
            if (parent instanceof DQLAdditiveExpression || parent instanceof DQLMultiplicativeExpression) {
                holder.registerProblem(
                        leftSide,
                        DQLBundle.message("inspection.rightSideNegativeValue.shouldBeParenthesized"),
                        new ParenthesizeElement()
                );
            }
        }
        if (rightSide instanceof DQLNegativeValueExpression) {
            holder.registerProblem(
                    rightSide,
                    DQLBundle.message("inspection.rightSideNegativeValue.shouldBeParenthesized"),
                    new ParenthesizeElement()
            );
        }
    }
}

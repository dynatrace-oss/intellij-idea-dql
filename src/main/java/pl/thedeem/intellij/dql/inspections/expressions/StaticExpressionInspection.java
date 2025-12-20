package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.*;

import java.util.List;

public class StaticExpressionInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitComparisonExpression(@NotNull DQLComparisonExpression expression) {
                super.visitComparisonExpression(expression);
                validateExpressionParts(expression, expression.getExpressionList(), holder);
            }

            @Override
            public void visitConditionExpression(@NotNull DQLConditionExpression expression) {
                super.visitConditionExpression(expression);
                validateExpressionParts(expression, expression.getExpressionList(), holder);
            }
        };
    }

    private static void validateExpressionParts(@NotNull DQLExpression expression, @NotNull List<DQLExpression> parts, @NotNull ProblemsHolder holder) {
        boolean onlyStatic = true;
        for (DQLExpression part : parts) {
            if (!(DQLUtil.unpackParenthesis(part) instanceof DQLPrimitiveExpression)) {
                onlyStatic = false;
                break;
            }
        }
        if (onlyStatic) {
            holder.registerProblem(expression, DQLBundle.message("inspection.expression.operand.onlyStatic"));
        }
    }
}

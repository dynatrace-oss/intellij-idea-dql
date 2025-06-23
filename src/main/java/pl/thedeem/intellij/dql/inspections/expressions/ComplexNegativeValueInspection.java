package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.fixes.SimplifyNegativeValueQuickFix;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLNegativeValueExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class ComplexNegativeValueInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitNegativeValueExpression(@NotNull DQLNegativeValueExpression expression) {
                super.visitNegativeValueExpression(expression);

                DQLExpression child = expression.getExpression();
                if (!(child instanceof DQLNegativeValueExpression)) {
                    DQLNegativeValueExpression topNegative = findFirstNegativeValue(expression);
                    if (topNegative != expression) {
                        holder.registerProblem(
                                topNegative,
                                DQLBundle.message("inspection.complexNegativeValue.canBeSimplified"),
                                new SimplifyNegativeValueQuickFix()
                        );
                    }
                }
            }
        };
    }

    private DQLNegativeValueExpression findFirstNegativeValue(@NotNull DQLNegativeValueExpression expression) {
        PsiElement element = expression;
        while (element.getParent() instanceof DQLNegativeValueExpression) {
            element = element.getParent();
        }
        return (DQLNegativeValueExpression) element;
    }
}

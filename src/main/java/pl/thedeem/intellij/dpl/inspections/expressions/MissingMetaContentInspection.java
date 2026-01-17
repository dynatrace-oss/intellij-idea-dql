package pl.thedeem.intellij.dpl.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLMetaExpression;
import pl.thedeem.intellij.dpl.psi.DPLMetaExpressionContent;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class MissingMetaContentInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitMetaExpression(@NotNull DPLMetaExpression expression) {
                super.visitMetaExpression(expression);

                DPLMetaExpressionContent content = expression.getMetaExpressionContent();
                if (content == null) {
                    holder.registerProblem(
                            expression,
                            DPLBundle.message("inspection.missingMetaContent.missing")
                    );
                }
            }
        };
    }
}

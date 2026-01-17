package pl.thedeem.intellij.dpl.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLExpression;
import pl.thedeem.intellij.dpl.psi.DPLMetaExpressionContent;
import pl.thedeem.intellij.dpl.psi.DPLSimpleExpression;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;
import pl.thedeem.intellij.dql.inspections.fixes.DropElementQuickFix;

public class InvalidMetaContentInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitMetaExpressionContent(@NotNull DPLMetaExpressionContent content) {
                super.visitMetaExpressionContent(content);

                DPLExpression expression = content.getExpression();
                if (expression == null) {
                    return;
                }
                if (!(expression instanceof DPLSimpleExpression)) {
                    holder.registerProblem(
                            expression,
                            DPLBundle.message("inspection.invalidMetaContent.invalidType"),
                            new DropElementQuickFix()
                    );
                }
            }
        };
    }
}

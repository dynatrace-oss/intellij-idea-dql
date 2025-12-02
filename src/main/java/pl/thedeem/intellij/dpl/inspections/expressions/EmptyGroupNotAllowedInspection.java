package pl.thedeem.intellij.dpl.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.DropGroupQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLGroupExpression;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class EmptyGroupNotAllowedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitGroupExpression(@NotNull DPLGroupExpression group) {
                super.visitGroupExpression(group);

                if (group.getExpressionsSequence() != null || group.getAlternativesExpression() != null) {
                    return;
                }

                holder.registerProblem(
                        group,
                        DPLBundle.message("inspection.command.emptyGroupNotAllowed"),
                        new DropGroupQuickFix()
                );
            }
        };
    }
}

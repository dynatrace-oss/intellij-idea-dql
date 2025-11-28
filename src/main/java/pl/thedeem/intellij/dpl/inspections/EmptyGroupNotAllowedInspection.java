package pl.thedeem.intellij.dpl.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.DropGroupQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLEmptyGroupExpression;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class EmptyGroupNotAllowedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitEmptyGroupExpression(@NotNull DPLEmptyGroupExpression group) {
                super.visitEmptyGroupExpression(group);

                holder.registerProblem(
                        group,
                        DPLBundle.message("inspection.command.emptyGroupNotAllowed"),
                        new DropGroupQuickFix()
                );
            }
        };
    }
}

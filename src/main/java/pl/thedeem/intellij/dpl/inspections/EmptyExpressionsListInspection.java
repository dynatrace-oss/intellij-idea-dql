package pl.thedeem.intellij.dpl.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.psi.DPLDpl;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class EmptyExpressionsListInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitDpl(@NotNull DPLDpl dpl) {
                super.visitDpl(dpl);

                if (dpl.getExpressionDefinitionList().isEmpty() && (dpl.getExpressionEnd() != null || !dpl.getMacroDefinitionExpressionList().isEmpty())) {
                    holder.registerProblem(dpl, DPLBundle.message("inspection.missingExpressionsList"));
                }
            }
        };
    }
}

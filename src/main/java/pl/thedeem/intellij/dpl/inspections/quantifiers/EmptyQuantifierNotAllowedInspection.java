package pl.thedeem.intellij.dpl.inspections.quantifiers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.DropQuantifierQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLEmptyQuantifier;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class EmptyQuantifierNotAllowedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitEmptyQuantifier(@NotNull DPLEmptyQuantifier quantifier) {
                super.visitEmptyQuantifier(quantifier);

                holder.registerProblem(
                        quantifier,
                        DPLBundle.message("inspection.command.emptyQuantifierNotAllowed"),
                        new DropQuantifierQuickFix()
                );
            }
        };
    }
}

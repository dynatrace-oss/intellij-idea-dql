package pl.thedeem.intellij.dpl.inspections.configuration;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.DropConfigurationParameterQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLInvalidParameter;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class InvalidConfigurationInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitInvalidParameter(@NotNull DPLInvalidParameter parameter) {
                super.visitInvalidParameter(parameter);

                holder.registerProblem(
                        parameter,
                        DPLBundle.message("inspection.invalidParameter", parameter.getText()),
                        ProblemHighlightType.ERROR,
                        new DropConfigurationParameterQuickFix()
                );
            }
        };
    }

}

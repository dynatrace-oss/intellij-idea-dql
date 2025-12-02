package pl.thedeem.intellij.dpl.inspections.configuration;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.DropConfigurationParameterQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLConfigurationContent;
import pl.thedeem.intellij.dpl.psi.DPLParameterExpression;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class ConfigurationNotAllowedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitParameterExpression(@NotNull DPLParameterExpression expression) {
                super.visitParameterExpression(expression);

                if (!(expression.getParent() instanceof DPLConfigurationContent)) {
                    holder.registerProblem(
                            expression,
                            DPLBundle.message("inspection.configurationParameterNotAllowed"),
                            new DropConfigurationParameterQuickFix()
                    );
                }
            }
        };
    }
}

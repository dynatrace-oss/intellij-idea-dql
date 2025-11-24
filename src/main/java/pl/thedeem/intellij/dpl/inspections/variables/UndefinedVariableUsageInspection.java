package pl.thedeem.intellij.dpl.inspections.variables;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.AddNewMacroQuickFix;
import pl.thedeem.intellij.dpl.inspections.fixes.DropVariableQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLVariable;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

import java.util.Objects;

public class UndefinedVariableUsageInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitVariable(@NotNull DPLVariable variable) {
                super.visitVariable(variable);

                if (variable.isDefinition()) {
                    return;
                }

                if (variable.getDefinition() == null) {
                    holder.registerProblem(
                            variable,
                            DPLBundle.message("inspection.variable.unknownVariable", variable.getName()),
                            new DropVariableQuickFix(),
                            new AddNewMacroQuickFix(Objects.requireNonNullElse(variable.getName(), "$var"))
                    );
                }
            }
        };
    }
}

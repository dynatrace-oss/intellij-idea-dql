package pl.thedeem.intellij.dpl.inspections;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.inspections.fixes.DropCommandQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLCommandExpression;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class UnknownCommandInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitCommandExpression(@NotNull DPLCommandExpression command) {
                super.visitCommandExpression(command);
                if (command.getDefinition() == null) {
                    holder.registerProblem(
                            command.getCommandKeyword(),
                            DPLBundle.message("inspection.command.unknown", command.getName()),
                            new DropCommandQuickFix()
                    );
                }
            }
        };
    }
}

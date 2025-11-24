package pl.thedeem.intellij.dpl.inspections.matchers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.inspections.fixes.DropMatchersQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLCommandExpression;
import pl.thedeem.intellij.dpl.psi.DPLCommandMatchers;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class MatchersNotSupportedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitCommandExpression(@NotNull DPLCommandExpression command) {
                super.visitCommandExpression(command);

                Command definition = command.getDefinition();
                if (definition == null) {
                    return;
                }

                DPLCommandMatchers matchers = command.getCommandMatchers();
                if (matchers == null) {
                    return;
                }

                if (definition.matchers() == null) {
                    holder.registerProblem(
                            matchers,
                            DPLBundle.message("inspection.command.matchersNotAllowed", command.getName()),
                            new DropMatchersQuickFix()
                    );
                }
            }
        };
    }
}

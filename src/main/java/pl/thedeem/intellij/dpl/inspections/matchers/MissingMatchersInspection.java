package pl.thedeem.intellij.dpl.inspections.matchers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.psi.DPLCommandExpression;
import pl.thedeem.intellij.dpl.psi.DPLCommandMatchers;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class MissingMatchersInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitCommandExpression(@NotNull DPLCommandExpression command) {
                super.visitCommandExpression(command);

                Command definition = command.getDefinition();
                if (definition == null || definition.matchers() == null || !definition.matchers().required()) {
                    return;
                }

                DPLCommandMatchers matchers = command.getCommandMatchers();
                if (matchers == null || matchers.getCommandMatchersContent() == null) {
                    holder.registerProblem(
                            command,
                            DPLBundle.message("inspection.command.missingMatchers", command.getName())
                    );
                }
            }
        };
    }
}

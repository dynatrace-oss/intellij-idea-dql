package pl.thedeem.intellij.dpl.inspections.quantifiers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.psi.*;

public class MissingQuantifierInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitExpressionDefinition(@NotNull DPLExpressionDefinition expression) {
                super.visitExpressionDefinition(expression);

                DPLQuantifier quantifier = expression.getQuantifier();
                if (quantifier != null) {
                    return;
                }

                DPLExpression expr = expression.getExpression();
                if (expr instanceof DPLCommandExpression command) {
                    validateCommand(command, holder);
                }
            }
        };
    }

    private void validateCommand(@NotNull DPLCommandExpression command, @NotNull ProblemsHolder holder) {
        Command definition = command.getDefinition();
        if (definition == null || definition.quantifier() == null || !definition.quantifier().required()) {
            return;
        }
        holder.registerProblem(
                command,
                DPLBundle.message("inspection.command.missingQuantifierForCommand", command.getName())
        );
    }
}

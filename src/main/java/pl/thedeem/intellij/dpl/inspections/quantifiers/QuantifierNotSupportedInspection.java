package pl.thedeem.intellij.dpl.inspections.quantifiers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.definition.model.Quantifier;
import pl.thedeem.intellij.dpl.inspections.fixes.DropQuantifierQuickFix;
import pl.thedeem.intellij.dpl.psi.*;

public class QuantifierNotSupportedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitExpressionDefinition(@NotNull DPLExpressionDefinition expression) {
                super.visitExpressionDefinition(expression);

                DPLQuantifier quantifier = expression.getQuantifier();
                if (quantifier == null) {
                    return;
                }
                DPLExpression expr = expression.getExpression();
                switch (expr) {
                    case DPLCommandExpression command -> validateCommand(command, quantifier, holder);
                    case DPLVariableUsageExpression ignored -> expressionUnsupported(quantifier, holder);
                    case DPLGroupExpression ignored -> expressionUnsupported(quantifier, holder);
                    default -> {
                    }
                }
            }
        };
    }

    private void validateCommand(@NotNull DPLCommandExpression command, @NotNull DPLQuantifier quantifier, @NotNull ProblemsHolder holder) {
        Command definition = command.getDefinition();
        if (definition == null) {
            return;
        }
        Quantifier commandQuantifier = definition.quantifier();
        if (commandQuantifier == null && quantifier instanceof DPLLimitedQuantifier limitedQuantifier) {
            holder.registerProblem(
                    limitedQuantifier,
                    DPLBundle.message("inspection.command.limitedQuantifierNotAllowed", command.getName()),
                    new DropQuantifierQuickFix()
            );
        }
    }

    private void expressionUnsupported(@NotNull DPLQuantifier quantifier, @NotNull ProblemsHolder holder) {
        holder.registerProblem(
                quantifier,
                DPLBundle.message("inspection.command.quantifierNotAllowedForExpression"),
                new DropQuantifierQuickFix()
        );
    }
}

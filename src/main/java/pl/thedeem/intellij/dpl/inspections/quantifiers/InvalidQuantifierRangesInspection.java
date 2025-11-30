package pl.thedeem.intellij.dpl.inspections.quantifiers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.definition.model.Quantifier;
import pl.thedeem.intellij.dpl.psi.*;
import pl.thedeem.intellij.dpl.psi.elements.QuantifierElement;

public class InvalidQuantifierRangesInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitExpressionDefinition(@NotNull DPLExpressionDefinition expression) {
                super.visitExpressionDefinition(expression);

                DPLQuantifierExpression quantifier = expression.getQuantifier();
                if (quantifier == null) {
                    return;
                }

                DPLExpression expr = expression.getExpression();
                if (expr instanceof DPLCommandExpression command) {
                    validateCommand(command, quantifier, holder);
                }
            }
        };
    }

    private void validateCommand(@NotNull DPLCommandExpression command, @NotNull DPLQuantifierExpression quantifier, @NotNull ProblemsHolder holder) {
        ExpressionDescription definition = command.getDefinition();
        if (definition == null || definition.quantifier() == null) {
            return;
        }
        Quantifier quantifierDef = definition.quantifier();

        long minValue = quantifierDef.min() != null ? quantifierDef.min() : 0L;
        QuantifierElement.MinMaxValues minMaxValues = quantifier.getMinMaxValues();

        if (quantifierDef.max() != null && minMaxValues.max() != null && quantifierDef.max() < minMaxValues.max()) {
            holder.registerProblem(
                    quantifier,
                    DPLBundle.message("inspection.command.invalidQuantifierRange.max", command.getName(), quantifierDef.max())
            );
        }
        if (minMaxValues.min() != null && minValue > minMaxValues.min()) {
            holder.registerProblem(
                    quantifier,
                    DPLBundle.message("inspection.command.invalidQuantifierRange.min", command.getName(), quantifierDef.min())
            );
        }
    }
}

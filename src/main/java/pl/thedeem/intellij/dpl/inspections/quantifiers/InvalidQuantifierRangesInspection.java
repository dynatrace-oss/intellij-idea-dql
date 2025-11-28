package pl.thedeem.intellij.dpl.inspections.quantifiers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLUtil;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.definition.model.Quantifier;
import pl.thedeem.intellij.dpl.psi.*;

public class InvalidQuantifierRangesInspection extends LocalInspectionTool {
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
                if (expr instanceof DPLCommandExpression command) {
                    validateCommand(command, quantifier, holder);
                }
            }
        };
    }

    private void validateCommand(@NotNull DPLCommandExpression command, @NotNull DPLQuantifier quantifier, @NotNull ProblemsHolder holder) {
        Command definition = command.getDefinition();
        if (definition == null || definition.quantifier() == null) {
            return;
        }
        Quantifier quantifierDef = definition.quantifier();

        long minValue = quantifierDef.min() != null ? quantifierDef.min() : 0L;
        DPLUtil.MinMaxValues minMaxValues = DPLUtil.getMinMaxValues(quantifier);

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

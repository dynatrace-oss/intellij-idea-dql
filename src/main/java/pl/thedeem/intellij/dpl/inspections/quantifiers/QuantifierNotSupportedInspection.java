package pl.thedeem.intellij.dpl.inspections.quantifiers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.definition.model.Quantifier;
import pl.thedeem.intellij.dpl.inspections.fixes.DropQuantifierQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLLimitedQuantifier;
import pl.thedeem.intellij.dpl.psi.DPLQuantifierExpression;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class QuantifierNotSupportedInspection extends LocalInspectionTool {
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
                ExpressionDescription definition = expression.getDefinition();
                if (definition == null) {
                    holder.registerProblem(
                            quantifier,
                            DPLBundle.message("inspection.command.quantifierNotAllowedForExpression"),
                            new DropQuantifierQuickFix()
                    );
                    return;
                }
                Quantifier quantifierDefinition = definition.quantifier();
                if (quantifierDefinition == null && quantifier.getQuantifierContent() instanceof DPLLimitedQuantifier limitedQuantifier) {
                    holder.registerProblem(
                            limitedQuantifier,
                            DPLBundle.message("inspection.command.limitedQuantifierNotAllowed"),
                            new DropQuantifierQuickFix()
                    );
                }

            }
        };
    }
}

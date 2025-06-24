package pl.thedeem.intellij.dql.inspections.simple;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.fixes.AddMissingVariableDefinitionQuickFix;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class UnknownVariableInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitVariableExpression(@NotNull DQLVariableExpression variable) {
                super.visitVariableExpression(variable);

                if (variable.getDefinition() == null) {
                    holder.registerProblem(variable, DQLBundle.message(
                            "inspection.variable.unknown.missingDefinition",
                            variable.getName()
                    ), new AddMissingVariableDefinitionQuickFix());
                }
            }
        };
    }
}

package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.inspections.fixes.AddBracketsToParameterValue;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class AdvisedBracketsAroundParametersInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLQueryStatement command) {
                    DQLParameterObject parameter = command.getParameter(expression);
                    if (parameter == null) {
                        return;
                    }
                    if (parameter.isNamed() && parameter.getValues().size() > 1) {
                        holder.registerProblem(
                                parameter.getNameIdentifier(),
                                DQLBundle.message("inspection.command.advisedBrackets.missingBrackets"),
                                new AddBracketsToParameterValue(parameter)
                        );
                    }
                }
            }
        };
    }
}

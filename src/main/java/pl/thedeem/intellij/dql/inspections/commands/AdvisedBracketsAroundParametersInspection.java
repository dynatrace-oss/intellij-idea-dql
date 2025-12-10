package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.fixes.AddBracketsToParameterValueQuickFix;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.List;

public class AdvisedBracketsAroundParametersInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLQueryStatement command) {
                    MappedParameter parameter = command.getParameter(expression);
                    Parameter definition = parameter != null ? parameter.definition() : null;
                    if (definition == null) {
                        return;
                    }
                    if (!parameter.holder().equals(expression)) {
                        return;
                    }
                    if (shouldAdviseBrackets(parameter, definition, command)) {
                        holder.registerProblem(
                                parameter.holder(),
                                DQLBundle.message("inspection.command.advisedBrackets.missingBrackets"),
                                new AddBracketsToParameterValueQuickFix(parameter)
                        );
                    }
                }
            }
        };
    }

    private boolean shouldAdviseBrackets(@NotNull MappedParameter parameter, @NotNull Parameter definition, @NotNull DQLQueryStatement parent) {
        if (!definition.variadic() || parameter.included().isEmpty()) {
            return false;
        }
        List<MappedParameter> variadicParams = parent.getParameters().stream().filter(p -> p.definition() != null && p.definition().variadic()).toList();
        return variadicParams.size() > 1;
    }
}

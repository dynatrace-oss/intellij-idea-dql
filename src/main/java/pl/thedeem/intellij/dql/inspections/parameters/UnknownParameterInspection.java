package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.inspections.fixes.AddParameterNameQuickFix;
import pl.thedeem.intellij.dql.inspections.fixes.DropInvalidParameterQuickFix;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

public class UnknownParameterInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);

                if (expression.getParent() instanceof DQLParametersOwner parametersOwner) {
                    DQLParameterObject parameter = parametersOwner.getParameter(expression);
                    DQLParameterDefinition definition = parameter != null ? parameter.getDefinition() : null;
                    if (definition == null) {
                        holder.registerProblem(
                                expression,
                                DQLBundle.message("inspection.parameter.unknown.unknownNamed"),
                                new DropInvalidParameterQuickFix(parameter)
                        );
                    } else if (parameter.isMissingName()) {
                        holder.registerProblem(
                                expression,
                                DQLBundle.message("inspection.parameter.unknown.unknownUnnamedParameter", definition.name),
                                new DropInvalidParameterQuickFix(parameter),
                                new AddParameterNameQuickFix(parameter)
                        );
                    }
                }
            }
        };
    }
}

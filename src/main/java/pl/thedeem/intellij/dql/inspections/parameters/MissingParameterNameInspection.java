package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.fixes.AddParameterNameQuickFix;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.model.MappedParameter;

public class MissingParameterNameInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLParametersOwner owner) {
                    MappedParameter parameter = owner.getParameter(expression);
                    Parameter definition = parameter != null ? parameter.definition() : null;
                    if (definition == null || definition.valueTypes().isEmpty()) {
                        return;
                    }

                    if (definition.requiresName() && !parameter.explicitlyNamed()) {
                        holder.registerProblem(
                                parameter.holder(),
                                DQLBundle.message(
                                        "inspection.parameter.parameterName.missingName",
                                        definition.name()
                                ),
                                new AddParameterNameQuickFix(parameter)
                        );
                    }
                }
            }
        };
    }
}

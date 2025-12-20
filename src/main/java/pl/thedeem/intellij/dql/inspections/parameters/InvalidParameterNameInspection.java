package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

public class InvalidParameterNameInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);
                if (expression.getParent() instanceof DQLParametersOwner parametersOwner) {
                    MappedParameter parameter = parametersOwner.getParameter(expression);
                    if (parameter != null) {
                        Parameter definition = parameter.definition();
                        if (definition != null && !definition.allowsName() && parameter.isExplicitlyNamed()) {
                            holder.registerProblem(
                                    expression,
                                    DQLBundle.message("inspection.parameter.invalidName.notAllowed", definition.name())
                            );
                        }
                    }
                }
            }
        };
    }
}

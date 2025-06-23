package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.inspections.fixes.DropInvalidParameterQuickFix;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

import java.util.Set;

public class ConflictedParameterInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);

                if (expression.getParent() instanceof DQLParametersOwner parametersOwner) {
                    Set<String> defined = parametersOwner.getDefinedParameterNames();
                    DQLParameterObject parameter = parametersOwner.getParameter(expression);

                    if (parameter != null) {
                        DQLParameterDefinition definition = parameter.getDefinition();
                        if (definition != null && DQLUtil.containsAny(defined, definition.getDisallowedParameters())) {
                            holder.registerProblem(
                                    expression,
                                    DQLBundle.message(
                                            "inspection.parameter.conflicted.conflictsWith",
                                            definition.name,
                                            DQLBundle.print(definition.getDisallowedParameters())
                                    ),
                                    new DropInvalidParameterQuickFix(parameter)
                            );
                        }
                    }
                }
            }
        };
    }
}

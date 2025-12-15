package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.inspections.fixes.DropInvalidParameterQuickFix;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

public class UnknownParameterInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);

                if (expression.getParent() instanceof DQLParametersOwner parametersOwner) {
                    MappedParameter parameter = parametersOwner.getParameter(expression);
                    Parameter definition = parameter != null ? parameter.definition() : null;
                    if (parameter != null && definition == null) {
                        holder.registerProblem(
                                parameter.holder() instanceof DQLParameterExpression p ? p.getParameterName() : parameter.holder(),
                                DQLBundle.message("inspection.parameter.unknown.unknownNamed"),
                                ProblemHighlightType.LIKE_UNKNOWN_SYMBOL,
                                new DropInvalidParameterQuickFix(parameter)
                        );
                    }
                }
            }
        };
    }
}

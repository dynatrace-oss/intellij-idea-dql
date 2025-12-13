package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.inspections.fixes.DropInvalidParameterQuickFix;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ConflictedParameterInspection extends BaseInspection {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitExpression(@NotNull DQLExpression expression) {
                super.visitExpression(expression);

                if (expression.getParent() instanceof DQLParametersOwner parametersOwner) {
                    MappedParameter parameter = parametersOwner.getParameter(expression);
                    Parameter definition = parameter != null ? parameter.definition() : null;
                    if (definition == null) {
                        return;
                    }

                    Set<String> parameters = parametersOwner.getParameters().stream().map(MappedParameter::name).collect(Collectors.toSet());
                    Set<String> disallowed = new HashSet<>();
                    if (parameter.definition().excludes() != null) {
                        disallowed.addAll(parameter.definition().excludes());
                    }

                    if (parameters.stream().anyMatch(disallowed::contains)) {
                        holder.registerProblem(
                                expression,
                                DQLBundle.message(
                                        "inspection.parameter.conflicted.conflictsWith",
                                        definition.name(),
                                        DQLBundle.print(disallowed)
                                ),
                                new DropInvalidParameterQuickFix(parameter)
                        );
                    }
                }
            }
        };
    }
}

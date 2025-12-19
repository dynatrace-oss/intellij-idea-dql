package pl.thedeem.intellij.dql.inspections.parameters;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.inspections.BaseInspection;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.ArrayList;
import java.util.List;

public class ParameterValueInspection extends BaseInspection {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitCommand(@NotNull DQLCommand expression) {
                super.visitCommand(expression);
                DQLParameterValueTypesValidator service = DQLParameterValueTypesValidator.getInstance(expression.getProject());
                for (MappedParameter parameter : expression.getParameters()) {
                    for (DQLParameterValueTypesValidator.ValueIssue issue : findIssues(parameter, service)) {
                        holder.registerProblem(issue.element(), issue.issue());
                    }
                }
            }

            @Override
            public void visitFunctionExpression(@NotNull DQLFunctionExpression expression) {
                super.visitFunctionExpression(expression);
                DQLParameterValueTypesValidator service = DQLParameterValueTypesValidator.getInstance(expression.getProject());
                for (MappedParameter parameter : expression.getParameters()) {
                    for (DQLParameterValueTypesValidator.ValueIssue issue : findIssues(parameter, service)) {
                        if (doesNotContainErrorToken(issue.element())) {
                            holder.registerProblem(issue.element(), issue.issue());
                        }
                    }
                }
            }
        };
    }

    private List<DQLParameterValueTypesValidator.ValueIssue> findIssues(@NotNull MappedParameter parameter, @NotNull DQLParameterValueTypesValidator service) {
        if (parameter.definition() == null) {
            return List.of();
        }
        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        for (PsiElement expression : parameter.getExpressions()) {
            result.addAll(service.validate(expression, parameter.definition()));
        }
        return result;
    }

}

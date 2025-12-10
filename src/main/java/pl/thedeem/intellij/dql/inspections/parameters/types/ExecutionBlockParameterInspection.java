package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLSubqueryExpression;

import java.util.List;

public class ExecutionBlockParameterInspection extends AbstractParameterValueTypeInspection {
    @Override
    protected void validateParameterValueType(@NotNull DQLExpression expression, @NotNull MappedParameter parameter, @NotNull Parameter definition, @NotNull List<String> parameterTypes, @NotNull ProblemsHolder holder) {
        if (parameterTypes.stream().noneMatch(DQLDefinitionService.EXECUTION_PARAMETER_VALUE_TYPES::contains)) {
            return;
        }
        for (PsiElement invalidValue : getInvalidValues(expression, element -> {
            PsiElement toProcess = element instanceof DQLAssignExpression assigned ? assigned.getRightExpression() : element;
            return !(toProcess instanceof DQLSubqueryExpression);
        })) {
            holder.registerProblem(
                    invalidValue,
                    DQLBundle.message("inspection.parameter.executionBlock.invalidValue", definition.name())
            );
        }
    }
}

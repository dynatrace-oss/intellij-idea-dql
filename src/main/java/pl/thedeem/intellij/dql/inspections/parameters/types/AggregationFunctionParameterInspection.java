package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class AggregationFunctionParameterInspection extends AbstractParameterValueTypeInspection {
    @Override
    protected void validateParameterValueType(@NotNull DQLExpression expression, @NotNull MappedParameter parameter, @NotNull Parameter definition, @NotNull List<String> parameterTypes, @NotNull ProblemsHolder holder) {
        if (parameterTypes.stream().noneMatch(Set.of(
                "dql.parameterValueType.expressionTimeseriesAggregation",
                "dql.parameterValueType.metricTimeseriesAggregation"
        )::contains)) {
            return;
        }

        DQLDefinitionService service = DQLDefinitionService.getInstance(expression.getProject());
        Collection<String> categories = service.getFunctionCategoriesForParameterTypes(parameterTypes);
        Collection<Function> allowedFunctions = categories != null ? service.getFunctionsByCategoryAndReturnType(
                s -> categories.isEmpty() || categories.contains(s),
                s -> definition.valueTypes() == null || definition.valueTypes().isEmpty() || definition.valueTypes().contains(s)
        ) : Set.of();
        for (PsiElement invalidValue : getInvalidValues(expression, element -> {
            if (element instanceof DQLAssignExpression assignExpression) {
                element = assignExpression.getRightExpression();
            }
            if (element instanceof DQLFunctionCallExpression functionCall) {
                Function funDef = functionCall.getDefinition();
                return funDef != null && !allowedFunctions.contains(funDef);
            }
            return true;
        })) {
            holder.registerProblem(
                    invalidValue,
                    DQLBundle.message(
                            "inspection.parameter.aggregationFunction.invalidFunction",
                            DQLBundle.print(allowedFunctions.stream().map(Function::name).toList())
                    )
            );
        }
    }
}

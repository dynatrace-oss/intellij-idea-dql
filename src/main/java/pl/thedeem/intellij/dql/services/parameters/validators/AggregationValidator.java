package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.OperatorElement;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.services.definition.model.Function;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class AggregationValidator extends AbstractParameterValidator {
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "dql.parameterValueType.expressionTimeseriesAggregation",
            "dql.parameterValueType.metricTimeseriesAggregation",
            "dql.parameterValueType.aggregation"
    );

    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        if (definition.parameterValueTypes().stream().noneMatch(ALLOWED_TYPES::contains)) {
            return List.of();
        }

        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        DQLDefinitionService service = DQLDefinitionService.getInstance(parameter.getProject());
        Collection<String> categories = service.getFunctionCategoriesForParameterTypes(definition.parameterValueTypes());
        Collection<Function> allowedFunctions = categories != null ? service.getFunctionsByCategoryAndReturnType(
                s -> categories.isEmpty() || categories.contains(s),
                s -> definition.valueTypes() == null || definition.valueTypes().isEmpty() || definition.valueTypes().contains(s)
        ) : Set.of();

        for (PsiElement invalidValue : getInvalidValues(parameter, element -> isInvalid(element, allowedFunctions))) {
            result.add(new DQLParameterValueTypesValidator.ValueIssue(
                    invalidValue,
                    DQLBundle.message(
                            "inspection.parameter.aggregationFunction.invalidFunction",
                            DQLBundle.print(allowedFunctions.stream().map(Function::name).toList())
                    )
            ));
        }
        return result;
    }

    private boolean isInvalid(PsiElement element, @NotNull Collection<Function> allowedFunctions) {
        List<PsiElement> toProcess = new ArrayList<>();
        toProcess.add(element);

        while (!toProcess.isEmpty()) {
            PsiElement processing = DQLUtil.unpackParenthesis(toProcess.removeFirst());

            switch (processing) {
                case OperatorElement expr -> toProcess.addAll(expr.getExpressions());
                case DQLNegativeValueExpression expr -> toProcess.add(expr.getExpression());
                case DQLUnaryExpression expr -> toProcess.add(expr.getExpression());
                case DQLParameterExpression expr -> toProcess.add(expr.getExpression());
                case DQLBracketExpression expr -> toProcess.addAll(expr.getExpressionList());
                case DQLFunctionExpression expr -> {
                    Function funDef = expr.getDefinition();
                    if (funDef == null || allowedFunctions.contains(funDef)) {
                        return false;
                    }
                    toProcess.addAll(expr.getExpressionList());
                }
                case null, default -> {
                }
            }
        }
        return true;
    }
}

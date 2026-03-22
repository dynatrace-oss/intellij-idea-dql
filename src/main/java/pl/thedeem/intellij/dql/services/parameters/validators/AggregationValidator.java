package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.eclipse.sisu.Nullable;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.*;
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
            List<PsiElement> unwinded = unwindExpression(processing);
            if (!unwinded.isEmpty()) {
                toProcess.addAll(unwinded);
            } else {
                switch (processing) {
                    case DQLFunctionExpression expr -> {
                        Function funDef = expr.getDefinition();
                        return funDef != null
                                && !allowedFunctions.contains(funDef)
                                && !containsAllowedAggregation(expr, allowedFunctions);
                    }
                    case null, default -> {
                        return true;
                    }
                }
            }
        }
        return true;
    }

    private boolean containsAllowedAggregation(@NotNull DQLFunctionExpression function, @NotNull Collection<Function> allowedFunctions) {
        List<PsiElement> toProcess = new ArrayList<>(function.getFunctionArguments());
        while (!toProcess.isEmpty()) {
            PsiElement processing = DQLUtil.unpackParenthesis(toProcess.removeFirst());
            List<PsiElement> unwinded = unwindExpression(processing);
            if (!unwinded.isEmpty()) {
                toProcess.addAll(unwinded);
            } else if (processing instanceof DQLFunctionExpression functionCall) {
                Function funDef = functionCall.getDefinition();
                // we do not know if the unknown function can be aggregation
                if (funDef == null) {
                    return true;
                }
                if (allowedFunctions.contains(funDef)) {
                    return true;
                }
                toProcess.addAll(functionCall.getFunctionArguments());
            }
        }
        return false;
    }

    private @NotNull List<PsiElement> unwindExpression(@Nullable PsiElement expression) {
        List<PsiElement> result = new ArrayList<>();

        switch (expression) {
            case DQLAssignExpression expr -> result.add(expr.getRightExpression());
            case DQLArithmeticalExpression expr -> {
                result.add(expr.getLeftExpression());
                result.add(expr.getRightExpression());
            }
            case DQLComparisonExpression expr -> {
                result.add(expr.getLeftExpression());
                result.add(expr.getRightExpression());
            }
            case DQLConditionExpression expr -> {
                result.add(expr.getLeftExpression());
                result.add(expr.getRightExpression());
            }
            case DQLEqualityExpression expr -> {
                result.add(expr.getLeftExpression());
                result.add(expr.getRightExpression());
            }
            case DQLInExpression expr -> result.add(expr.getLeftExpression());
            case DQLNegativeValueExpression expr -> result.add(expr.getExpression());
            case DQLUnaryExpression expr -> result.add(expr.getExpression());
            case DQLArrayExpression expr -> result.add(expr.getBaseExpression());
            case DQLParameterExpression expr -> result.add(expr.getExpression());
            case DQLBracketExpression expr -> result.addAll(expr.getExpressionList());
            case null, default -> {
            }
        }

        return result;
    }
}

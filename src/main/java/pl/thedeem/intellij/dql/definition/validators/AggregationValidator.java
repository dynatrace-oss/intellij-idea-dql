package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class AggregationValidator extends AbstractDefinitionValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        DQLDefinitionService service = DQLDefinitionService.getInstance(parameter.getProject());
        Collection<String> categories = service.getFunctionCategoriesForParameterTypes(definition.parameterValueTypes());
        Collection<Function> allowedFunctions = categories != null ? service.getFunctionsByCategoryAndReturnType(
                s -> categories.isEmpty() || categories.contains(s),
                s -> definition.valueTypes() == null || definition.valueTypes().isEmpty() || definition.valueTypes().contains(s)
        ) : Set.of();
        for (PsiElement invalidValue : getInvalidValues(parameter, element -> {
            if (element instanceof DQLAssignExpression assignExpression) {
                element = assignExpression.getRightExpression();
            }
            if (element instanceof DQLFunctionExpression functionCall) {
                Function funDef = functionCall.getDefinition();
                return funDef != null && !allowedFunctions.contains(funDef);
            }
            return true;
        })) {
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
}

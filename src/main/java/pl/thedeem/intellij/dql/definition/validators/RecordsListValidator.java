package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RecordsListValidator extends AbstractDefinitionValidator {
    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<DQLParameterValueTypesValidator.ValueIssue> issues = new ArrayList<>();
        DQLDefinitionService service = DQLDefinitionService.getInstance(parameter.getProject());
        Collection<String> categories = service.getFunctionCategoriesForParameterTypes(definition.parameterValueTypes());
        Collection<Function> allowedFunctions = categories != null ? service.getFunctionsByCategoryAndReturnType(
                s -> categories.isEmpty() || categories.contains(s),
                "dql.dataType.record"::equals
        ) : Set.of();
        for (PsiElement invalidValue : getInvalidValues(parameter,
                element -> {
                    if (!(element instanceof DQLFunctionCallExpression function)) {
                        return true;
                    }

                    Function funDef = function.getDefinition();
                    return funDef == null || !allowedFunctions.contains(funDef);
                })
        ) {
            issues.add(new DQLParameterValueTypesValidator.ValueIssue(
                    invalidValue,
                    DQLBundle.message(
                            "inspection.parameter.recordsList.invalidFunction",
                            DQLBundle.print(allowedFunctions.stream().map(Function::name).toList()))
            ));
        }
        return issues;
    }
}

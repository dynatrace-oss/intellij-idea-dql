package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RecordsListParameterInspection extends AbstractParameterValueTypeInspection {
    @Override
    protected void validateParameterValueType(@NotNull DQLExpression expression, @NotNull MappedParameter parameter, @NotNull Parameter definition, @NotNull List<String> parameterTypes, @NotNull ProblemsHolder holder) {
        if (!parameterTypes.contains("dql.parameterValueType.recordDefinition")) {
            return;
        }

        DQLDefinitionService service = DQLDefinitionService.getInstance(parameter.holder().getProject());
        Collection<String> categories = service.getFunctionCategoriesForParameterTypes(parameterTypes);
        Collection<Function> allowedFunctions = categories != null ? service.getFunctionsByCategoryAndReturnType(
                s -> categories.isEmpty() || categories.contains(s),
                "dql.dataType.record"::equals
        ) : Set.of();
        for (PsiElement invalidValue : getInvalidValues(expression,
                element -> {
                    if (!(element instanceof DQLFunctionCallExpression function)) {
                        return true;
                    }

                    Function funDef = function.getDefinition();
                    return funDef == null || !allowedFunctions.contains(funDef);
                })
        ) {
            holder.registerProblem(
                    invalidValue,
                    DQLBundle.message(
                            "inspection.parameter.recordsList.invalidFunction",
                            DQLBundle.print(allowedFunctions.stream().map(Function::name).toList()))
            );
        }
    }
}

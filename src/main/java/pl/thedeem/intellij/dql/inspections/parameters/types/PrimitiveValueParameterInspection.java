package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLSimpleExpression;

import java.util.List;

public class PrimitiveValueParameterInspection extends AbstractParameterValueTypeInspection {
    @Override
    protected void validateParameterValueType(@NotNull DQLExpression expression, @NotNull MappedParameter parameter, @NotNull Parameter definition, @NotNull List<String> parameterTypes, @NotNull ProblemsHolder holder) {
        if (!parameterTypes.contains("dql.parameterValueType.primitiveValue")) {
            return;
        }
        for (PsiElement invalidValue : getInvalidValues(expression, element -> !(element instanceof DQLSimpleExpression))) {
            holder.registerProblem(
                    invalidValue,
                    DQLBundle.message("inspection.parameter.primitiveValue.invalidValue", definition.name(), DQLBundle.types(definition.valueTypes(), invalidValue.getProject()))
            );
        }
    }
}

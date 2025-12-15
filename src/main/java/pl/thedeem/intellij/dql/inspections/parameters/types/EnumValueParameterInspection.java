package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;

import java.util.List;

public class EnumValueParameterInspection extends AbstractParameterValueTypeInspection {
    @Override
    protected void validateParameterValueType(
            @NotNull DQLExpression expression,
            @NotNull MappedParameter parameter,
            @NotNull Parameter definition,
            @NotNull List<String> parameterTypes,
            @NotNull ProblemsHolder holder
    ) {
        if (!parameterTypes.contains("dql.parameterValueType.enum") || definition.allowedEnumValues() == null) {
            return;
        }
        for (PsiElement invalidValue : getInvalidValues(expression, e -> !definition.allowedEnumValues().contains(e.getText().trim()))) {
            holder.registerProblem(
                    invalidValue,
                    DQLBundle.message(
                            "inspection.parameter.enumValue.invalidValue",
                            definition.name(),
                            DQLBundle.print(definition.allowedEnumValues())
                    )
            );
        }
    }
}

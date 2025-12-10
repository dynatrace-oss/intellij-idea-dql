package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ConstantValueParameterInspection extends AbstractParameterValueTypeInspection {
    @Override
    protected void validateParameterValueType(
            @NotNull DQLExpression expression,
            @NotNull MappedParameter parameter,
            @NotNull Parameter definition,
            @NotNull List<String> parameterTypes,
            @NotNull ProblemsHolder holder) {
        if (!parameterTypes.contains("dql.parameterValueType.expressionWithConstantValue")) {
            return;
        }
        if (expression instanceof BaseElement element && element.accessesData() && doesNotContainErrorToken(element)) {
            holder.registerProblem(
                    expression,
                    DQLBundle.message(
                            "inspection.parameter.constantValue.invalidValue",
                            definition.name(),
                            DQLBundle.types(Objects.requireNonNullElse(definition.valueTypes(), Set.of()), expression.getProject())
                    )
            );
        }
    }
}

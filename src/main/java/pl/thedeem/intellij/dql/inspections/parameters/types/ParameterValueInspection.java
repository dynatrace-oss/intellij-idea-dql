package pl.thedeem.intellij.dql.inspections.parameters.types;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class ParameterValueInspection extends AbstractParameterValueTypeInspection {
    private final static Set<String> IGNORED_TYPES = Set.of("dql.dataType.null", "dql.dataType.array");

    @Override
    protected void validateParameterValueType(@NotNull DQLExpression expression, @NotNull MappedParameter parameter, @NotNull Parameter definition, @NotNull List<String> parameterTypes, @NotNull ProblemsHolder holder) {
        if (definition.valueTypes() == null || definition.valueTypes().isEmpty()) {
            return;
        }
        for (PsiElement invalidValue : getInvalidValues(expression, e -> isElementInvalid(e, definition))) {
            holder.registerProblem(
                    invalidValue,
                    DQLBundle.message(
                            "inspection.command.parametersValue.invalidValue",
                            definition.name(),
                            DQLBundle.types(definition.valueTypes(), invalidValue.getProject()),
                            DQLBundle.types(parameter.getDataType(), invalidValue.getProject())
                    )
            );
        }
    }

    private boolean isElementInvalid(@Nullable PsiElement e, @NotNull Parameter definition) {
        if (e instanceof BaseElement entity) {
            Collection<String> returned = entity.getDataType();
            Collection<String> defined = definition.valueTypes();

            if (returned.isEmpty() || defined.isEmpty()) {
                return false;
            }

            if (returned.stream().anyMatch(IGNORED_TYPES::contains) || defined.stream().anyMatch(IGNORED_TYPES::contains)) {
                return false;
            }

            return definition.valueTypes().stream().noneMatch(returned::contains);
        }
        return false;
    }
}

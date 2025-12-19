package pl.thedeem.intellij.dql.definition.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.DQLParameterValueTypesValidator;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.elements.BaseElement;

import java.util.*;

public class ValueValidator extends AbstractDefinitionValidator {
    private final static Set<String> IGNORED_TYPES = Set.of(
            "dql.dataType.null", // null values can be also representing any other values
            "dql.dataType.array" // for now, array are not supported for validations due to iterative expressions
    );

    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        List<String> wantedValues = Objects.requireNonNull(definition.valueTypes());
        if (wantedValues.isEmpty()) {
            return List.of();
        }
        for (PsiElement invalidValue : getInvalidValues(parameter, e -> isElementInvalid(e, wantedValues))) {
            if (invalidValue instanceof BaseElement entity) {
                result.add(new DQLParameterValueTypesValidator.ValueIssue(
                        invalidValue,
                        DQLBundle.message(
                                "inspection.command.parametersValue.invalidValue",
                                definition.name(),
                                DQLBundle.types(wantedValues, invalidValue.getProject()),
                                DQLBundle.types(entity.getDataType(), invalidValue.getProject())
                        )
                ));
            }
        }
        return result;
    }

    private boolean isElementInvalid(@Nullable PsiElement e, @NotNull List<String> wantedValues) {
        if (e instanceof BaseElement entity) {
            Collection<String> returned = entity.getDataType();
            if (returned.isEmpty()) {
                return false;
            }
            if (returned.stream().anyMatch(IGNORED_TYPES::contains) || wantedValues.stream().anyMatch(IGNORED_TYPES::contains)) {
                return false;
            }
            return wantedValues.stream().noneMatch(returned::contains);
        }
        return false;
    }
}

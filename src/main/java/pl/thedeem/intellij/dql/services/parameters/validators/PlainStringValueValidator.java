package pl.thedeem.intellij.dql.services.parameters.validators;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLString;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.DQLParameterValueTypesValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlainStringValueValidator extends AbstractParameterValidator {
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "dql.parameterValueType.bucket",
            "dql.parameterValueType.dplPattern",
            "dql.parameterValueType.filePattern",
            "dql.parameterValueType.jsonPath",
            "dql.parameterValueType.namelessDplPattern",
            "dql.parameterValueType.prefix",
            "dql.parameterValueType.tabularFileExisting",
            "dql.parameterValueType.tabularFileNew",
            "dql.parameterValueType.url"
    );

    @Override
    public @NotNull List<DQLParameterValueTypesValidator.ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        if (definition.parameterValueTypes().stream().noneMatch(ALLOWED_TYPES::contains)) {
            return List.of();
        }

        List<DQLParameterValueTypesValidator.ValueIssue> result = new ArrayList<>();
        for (PsiElement invalidValue : getInvalidValues(parameter, element -> !(element instanceof DQLString) && !(element instanceof DQLVariableExpression))) {
            result.add(new DQLParameterValueTypesValidator.ValueIssue(
                            invalidValue,
                            DQLBundle.message(
                                    "inspection.parameter.plainString.invalidValue",
                                    definition.name())
                    )
            );
        }
        return result;
    }
}

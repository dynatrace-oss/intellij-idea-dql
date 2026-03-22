package pl.thedeem.intellij.dql.services.parameters;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.parameters.validators.*;

import java.util.ArrayList;
import java.util.List;

public class DQLParameterValueTypesValidatorImpl implements DQLParameterValueTypesValidator {
    private static final List<ParameterValidator> validators = List.of(
            new ConstantValueValidator(),
            new PrimitiveValueValidator(),
            new SubqueryValidator(),
            new NonEmptySubqueryValidator(),
            new EnumValuesValidator(),
            new AggregationValidator(),
            new JoinConditionValidator(),
            new FieldIdentifierValidator(),
            new RecordsListValidator(),
            new PlainStringValueValidator(),
            new ValueValidator()
    );

    @Override
    public @NotNull List<ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<ValueIssue> result = new ArrayList<>();
        for (ParameterValidator validator : validators) {
            result.addAll(validator.validate(parameter, definition));
        }
        return result;
    }
}

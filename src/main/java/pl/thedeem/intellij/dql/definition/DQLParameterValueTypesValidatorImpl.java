package pl.thedeem.intellij.dql.definition;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.validators.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DQLParameterValueTypesValidatorImpl implements DQLParameterValueTypesValidator {
    @Override
    public @NotNull List<ValueIssue> validate(@NotNull PsiElement parameter, @NotNull Parameter definition) {
        List<ValueIssue> result = new ArrayList<>();
        List<String> types = Objects.requireNonNullElse(definition.parameterValueTypes(), List.of());
        List<String> values = Objects.requireNonNullElse(definition.valueTypes(), List.of());
        if (!types.isEmpty()) {
            result.addAll(validateTypes(parameter, types, definition));
        }
        if (!values.isEmpty()) {
            result.addAll(new ValueValidator().validate(parameter, definition));
        }
        return result;
    }

    private @NotNull List<ValueIssue> validateTypes(@NotNull PsiElement parameter, @NotNull List<String> types, @NotNull Parameter definition) {
        List<ValueIssue> result = new ArrayList<>();
        for (String type : types) {
            result.addAll(switch (type) {
                case "dql.parameterValueType.primitiveValue" ->
                        new PrimitiveValueValidator().validate(parameter, definition);
                case "dql.parameterValueType.expressionWithConstantValue" ->
                        new ConstantValueValidator().validate(parameter, definition);
                case "dql.parameterValueType.executionBlock" -> new SubqueryValidator().validate(parameter, definition);
                case "dql.parameterValueType.nonEmptyExecutionBlock" ->
                        new NonEmptySubqueryValidator().validate(parameter, definition);
                case "dql.parameterValueType.enum" -> new EnumValuesValidator().validate(parameter, definition);
                case "dql.parameterValueType.expressionTimeseriesAggregation",
                     "dql.parameterValueType.metricTimeseriesAggregation" ->
                        new AggregationValidator().validate(parameter, definition);
                case "dql.parameterValueType.joinCondition" ->
                        new JoinConditionValidator().validate(parameter, definition);
                case "dql.parameterValueType.fieldPattern",
                     "dql.parameterValueType.identifierForFieldOnRootLevel",
                     "dql.parameterValueType.identifierForAnyField",
                     "dql.parameterValueType.dataObject" ->
                        new FieldIdentifierValidator().validate(parameter, definition);
                case "dql.parameterValueType.recordDefinition" ->
                        new RecordsListValidator().validate(parameter, definition);
                default -> List.of();
            });
        }
        return result;
    }
}

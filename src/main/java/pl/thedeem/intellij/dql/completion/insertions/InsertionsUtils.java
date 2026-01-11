package pl.thedeem.intellij.dql.completion.insertions;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;

import java.util.List;
import java.util.Objects;

public class InsertionsUtils {
    public static void handleInsertionDefaultValue(@NotNull Parameter parameter, @NotNull Template template) {
        String name = parameter.name();
        if (handleByParameterType(parameter, template, name)) {
            return;
        }
        if (handleByParameterValue(parameter, template, name)) {
            return;
        }

        template.addVariable("bodyEmpty:" + name, new TextExpression(parameter.name()), new EmptyNode(), true);
    }

    private static boolean handleByParameterType(@NotNull Parameter parameter, @NotNull Template template, @NotNull String name) {
        List<String> parameterValueTypes = Objects.requireNonNullElse(parameter.parameterValueTypes(), List.of());
        if (parameter.defaultValue() != null) {
            template.addVariable("bodyDefaultValue:" + name, new TextExpression(parameter.defaultValue()), new EmptyNode(), true);
            return true;
        }

        if (parameterValueTypes.stream().anyMatch(DQLDefinitionService.FIELD_IDENTIFIER_PARAMETER_VALUE_TYPES::contains)) {
            template.addVariable("fieldName:" + name, new TextExpression("field"), new EmptyNode(), true);
            return true;
        }
        if (parameterValueTypes.contains("dql.parameterValueType.expressionWithFieldAccess")) {
            template.addVariable("fieldName:" + name, new TextExpression("field"), new EmptyNode(), true);
            return true;
        }
        if (parameterValueTypes.stream().anyMatch(DQLDefinitionService.DPL_VALUE_TYPES::contains)) {
            template.addTextSegment("\"\"\"");
            template.addVariable("dplPattern:" + name, new TextExpression(""), new EmptyNode(), true);
            template.addTextSegment("\"\"\"");
            return true;
        }

        if (parameterValueTypes.contains("dql.dataType.json")) {
            template.addTextSegment("\"\"\"");
            template.addVariable("json:" + name, new TextExpression(""), new EmptyNode(), true);
            template.addTextSegment("\"\"\"");
            return true;
        }

        if (parameterValueTypes.stream().anyMatch(DQLDefinitionService.EXECUTION_PARAMETER_VALUE_TYPES::contains)) {
            template.addTextSegment("[]");
            return true;
        }

        if (parameterValueTypes.stream().anyMatch(DQLDefinitionService.STRING_PARAMETER_VALUE_TYPES::contains)) {
            template.addTextSegment("\"");
            template.addVariable("bodyStringValue:" + name, new TextExpression(""), new EmptyNode(), true);
            template.addTextSegment("\"");
            return true;
        }

        if (parameterValueTypes.contains("dql.parameterValueType.joinCondition")) {
            template.addTextSegment("left[");
            template.addVariable("bodyLeftSideExpression:" + name, new TextExpression("field"), new EmptyNode(), true);
            template.addTextSegment("] == right[");
            template.addVariable("bodyRightSideExpression:" + name, new TextExpression("field"), new EmptyNode(), true);
            template.addTextSegment("]");
            return true;
        }

        if (parameter.requiresFieldName()) {
            template.addVariable("bodyDataAssignField:" + name, new TextExpression(""), new EmptyNode(), true);
            template.addTextSegment(" = ");
            template.addVariable("bodyDataAssignValue:" + name, new EmptyNode(), new EmptyNode(), true);
            return true;
        }

        if (parameter.variadic() && parameter.requiresName()) {
            template.addTextSegment("{ ");
            template.addVariable("bodyStatementExpression:" + name, new EmptyNode(), new EmptyNode(), true);
            template.addTextSegment(" }");
            return true;
        }

        return false;
    }

    private static boolean handleByParameterValue(@NotNull Parameter parameter, @NotNull Template template, @NotNull String name) {
        List<String> valueTypes = Objects.requireNonNullElse(parameter.valueTypes(), List.of());

        // We assume that the more important value types are presented on top of the list
        for (String valueType : valueTypes) {
            if ("dql.dataType.boolean".equals(valueType)) {
                template.addVariable("bodyBooleanValue:" + name, new TextExpression("true"), new EmptyNode(), true);
                return true;
            }
            if ("dql.dataType.timestamp".equals(valueType)) {
                template.addVariable("bodyTimestampValue:" + name, new TextExpression("now()"), new EmptyNode(), true);
                return true;
            }
            if ("dql.dataType.duration".equals(valueType)) {
                template.addVariable("bodyDurationValue:" + name, new TextExpression("-4h"), new EmptyNode(), true);
                return true;
            }
            if (DQLDefinitionService.NUMERIC_VALUE_TYPES.contains(valueType)) {
                template.addVariable("bodyNumericValue:" + name, new TextExpression("1"), new EmptyNode(), true);
                return true;
            }
            if (DQLDefinitionService.STRING_VALUE_TYPES.contains(valueType)) {
                template.addTextSegment("\"");
                template.addVariable("bodyStringValue:" + name, new TextExpression(""), new EmptyNode(), true);
                template.addTextSegment("\"");
                return true;
            }
        }

        return false;
    }
}

package pl.thedeem.intellij.dql.completion.insertions;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.services.definition.model.Parameter;

import java.util.List;

public class InsertionsUtils {
    public static void handleInsertionDefaultValue(@NotNull Project project, @NotNull Parameter parameter, @NotNull Template template) {
        String name = parameter.name();
        if (handleByParameterType(project, parameter, template, name)) {
            return;
        }
        if (handleByParameterValue(project, parameter, template, name)) {
            return;
        }

        template.addVariable("bodyEmpty:" + name, new TextExpression(parameter.name()), new EmptyNode(), true);
    }

    private static boolean handleByParameterType(@NotNull Project project, @NotNull Parameter parameter, @NotNull Template template, @NotNull String name) {
        DQLDefinitionService service = DQLDefinitionService.getInstance(project);
        List<String> parameterValueTypes = parameter.parameterValueTypes();
        if (parameter.defaultValue() != null) {
            template.addVariable("bodyDefaultValue:" + name, new TextExpression(parameter.defaultValue()), new EmptyNode(), true);
            return true;
        }

        if (parameterValueTypes.stream().anyMatch(service.getFieldIdentifierParameterValueTypes()::contains)) {
            template.addVariable("fieldName:" + name, new TextExpression("field"), new EmptyNode(), true);
            return true;
        }
        if (parameterValueTypes.contains("dql.parameterValueType.expressionWithFieldAccess")) {
            template.addVariable("fieldName:" + name, new TextExpression("field"), new EmptyNode(), true);
            return true;
        }
        if (parameterValueTypes.stream().anyMatch(service.getDplParameterValueTypes()::contains)) {
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

        if (parameterValueTypes.stream().anyMatch(service.getExecutionBlockParameterValueTypes()::contains)) {
            template.addTextSegment("[]");
            return true;
        }

        if (parameterValueTypes.stream().anyMatch(service.getStringParameterValueTypes()::contains)) {
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

        if (parameterValueTypes.contains("dql.parameterValueType.recordDefinition")) {
            template.addTextSegment("record(");
            template.addVariable("recordDefinition:" + name, new EmptyNode(), new EmptyNode(), true);
            template.addTextSegment(")");
            return true;
        }

        return false;
    }

    private static boolean handleByParameterValue(@NotNull Project project, @NotNull Parameter parameter, @NotNull Template template, @NotNull String name) {
        DQLDefinitionService service = DQLDefinitionService.getInstance(project);
        List<String> valueTypes = parameter.valueTypes();

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
            if (service.getNumericDataTypes().contains(valueType)) {
                template.addVariable("bodyNumericValue:" + name, new TextExpression("1"), new EmptyNode(), true);
                return true;
            }
            if (service.getStringDataTypes().contains(valueType)) {
                template.addTextSegment("\"");
                template.addVariable("bodyStringValue:" + name, new TextExpression(""), new EmptyNode(), true);
                template.addTextSegment("\"");
                return true;
            }
        }

        return false;
    }
}

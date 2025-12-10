package pl.thedeem.intellij.dql.completion.insertions;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.util.List;
import java.util.Objects;

public class InsertionsUtils {
    public static void handleInsertionDefaultValue(@NotNull Parameter parameter, @NotNull Template template) {
        List<String> valueTypes = Objects.requireNonNullElse(parameter.valueTypes(), List.of());
        List<String> parameterValueTypes = Objects.requireNonNullElse(parameter.parameterValueTypes(), List.of());
        String name = parameter.name();

        if (parameter.defaultValue() != null) {
            template.addVariable("bodyDefaultValue:" + name, new TextExpression(parameter.defaultValue()), new EmptyNode(), true);
            return;
        }

        if (parameterValueTypes.stream().anyMatch(DQLDefinitionService.FIELD_IDENTIFIER_PARAMETER_VALUE_TYPES::contains)) {
            template.addVariable("fieldName:" + name, new TextExpression("field"), new EmptyNode(), true);
            return;
        }
        if (parameterValueTypes.stream().anyMatch(DQLDefinitionService.DPL_VALUE_TYPES::contains)) {
            template.addTextSegment("\"\"\"");
            template.addVariable("dplPattern:" + name, new TextExpression(""), new EmptyNode(), true);
            template.addTextSegment("\"\"\"");
            return;
        }

        if (parameterValueTypes.contains("dql.dataType.json")) {
            template.addTextSegment("\"\"\"");
            template.addVariable("json:" + name, new TextExpression(""), new EmptyNode(), true);
            template.addTextSegment("\"\"\"");
            return;
        }

        if (parameterValueTypes.stream().anyMatch(DQLDefinitionService.EXECUTION_PARAMETER_VALUE_TYPES::contains)) {
            template.addTextSegment("[]");
            return;
        }

        if (parameterValueTypes.stream().anyMatch(DQLDefinitionService.STRING_PARAMETER_VALUE_TYPES::contains)) {
            template.addTextSegment("\"");
            template.addVariable("bodyStringValue:" + name, new TextExpression(""), new EmptyNode(), true);
            template.addTextSegment("\"");
            return;
        }

        if (parameterValueTypes.contains("dql.parameterValueType.joinCondition")) {
            template.addTextSegment("left[");
            template.addVariable("bodyLeftSideExpression:" + name, new TextExpression("field"), new EmptyNode(), true);
            template.addTextSegment("] == right[");
            template.addVariable("bodyRightSideExpression:" + name, new TextExpression("field"), new EmptyNode(), true);
            template.addTextSegment("]");
            return;
        }

        if ("mandatory".equals(parameter.assignmentSupport())) {
            template.addVariable("bodyDataAssignField:" + name, new TextExpression(""), new EmptyNode(), true);
            template.addTextSegment(" = ");
            template.addVariable("bodyDataAssignValue:" + name, new EmptyNode(), new EmptyNode(), true);
            return;
        }

        if (parameter.variadic()) {
            template.addTextSegment("{ ");
            template.addVariable("bodyStatementExpression:" + name, new EmptyNode(), new EmptyNode(), true);
            template.addTextSegment(" }");
            return;
        }

        if (valueTypes.stream().anyMatch(DQLDefinitionService.STRING_VALUE_TYPES::contains)) {
            template.addTextSegment("\"");
            template.addVariable("bodyStringValue:" + name, new TextExpression(""), new EmptyNode(), true);
            template.addTextSegment("\"");
            return;
        }

        template.addVariable("bodyEmpty:" + name, new TextExpression(""), new EmptyNode(), true);
    }
}

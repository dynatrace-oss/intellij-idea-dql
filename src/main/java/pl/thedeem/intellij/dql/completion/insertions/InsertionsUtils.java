package pl.thedeem.intellij.dql.completion.insertions;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.util.text.StringUtil;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.Set;

public class InsertionsUtils {
    public static void handleInsertionDefaultValue(Set<DQLDataType> allowedValues, Template template, String defaultValue, String name) {
        if (StringUtil.isNotEmpty(defaultValue)) {
            template.addVariable("bodyDefaultValue:" + name, new TextExpression(defaultValue), new EmptyNode(), true);
            return;
        }
        if (allowedValues == null || allowedValues.isEmpty()) {
            template.addVariable("bodyUnknownType:" + name, new EmptyNode(), new EmptyNode(), true);
            return;
        }
        if (allowedValues.contains(DQLDataType.NAMED_SUBQUERY_EXPRESSION)) {
            template.addVariable("bodySubqueryFieldName:" + name, new TextExpression("some.field"), new EmptyNode(), true);
            template.addTextSegment(" = []");
        }
        else if (allowedValues.contains(DQLDataType.JOIN_CONDITION)) {
            template.addTextSegment("left[");
            template.addVariable("bodyLeftSideExpression:" + name, new TextExpression("field"), new EmptyNode(), true);
            template.addTextSegment("] == right[");
            template.addVariable("bodyRightSideExpression:" + name, new TextExpression("field"), new EmptyNode(), true);
            template.addTextSegment("]");
        }
        else if (allowedValues.contains(DQLDataType.SUBQUERY_EXPRESSION)) {
            template.addTextSegment("[]");
        } else if (allowedValues.contains(DQLDataType.LIST_OF_EXPRESSIONS)
                || allowedValues.contains(DQLDataType.READ_ONLY_EXPRESSION)
                || allowedValues.contains(DQLDataType.WRITE_ONLY_EXPRESSION)) {
            template.addTextSegment("{ ");
            template.addVariable("bodyStatementExpression:" + name, new EmptyNode(), new EmptyNode(), true);
            template.addTextSegment(" }");
        } else if (allowedValues.contains(DQLDataType.IDENTIFIER)) {
            template.addVariable("bodyDataObject:" + name, new TextExpression("some.field"), new EmptyNode(), true);
        } else if (allowedValues.contains(DQLDataType.ASSIGN_EXPRESSION)) {
            template.addVariable("bodyDataAssignField:" + name, new TextExpression("some.field"), new EmptyNode(), true);
            template.addTextSegment(" = ");
            template.addVariable("bodyDataAssignValue:" + name, new EmptyNode(), new EmptyNode(), true);
        } else if (allowedValues.contains(DQLDataType.NUMBER)) {
            template.addVariable("bodyNumericalType:" + name, new EmptyNode(), new EmptyNode(), true);
        } else if (allowedValues.contains(DQLDataType.STRING)) {
            template.addVariable("bodyStringType:" + name, new TextExpression("\"\""), new EmptyNode(), true);
        } else {
            template.addVariable("bodyStatementSimple:" + name, new EmptyNode(), new EmptyNode(), true);
        }
    }
}

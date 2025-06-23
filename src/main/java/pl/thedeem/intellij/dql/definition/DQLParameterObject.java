package pl.thedeem.intellij.dql.definition;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.psi.DQLBracketExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;

import java.util.List;
import java.util.Set;

public class DQLParameterObject implements BaseTypedElement {
    private final DQLParameterDefinition definition;
    private final DQLExpression expression;
    private final List<DQLExpression> values;

    public DQLParameterObject(DQLParameterDefinition definition, DQLExpression expression, List<DQLExpression> relatedObjects) {
        this.definition = definition;
        this.expression = expression;
        this.values = relatedObjects;
    }

    public @Nullable TextRange getTextRange() {
        if (expression != null) {
            int start = expression.getTextRange().getStartOffset();
            int end = expression.getTextRange().getEndOffset();

            if (!values.isEmpty()) {
                start = values.getFirst().getTextRange().getStartOffset();
                end = values.getLast().getTextRange().getEndOffset();
            }
            return new TextRange(start, end);
        }
        return null;
    }

    public @NotNull TextRange getTextRange(TextRange defaultRange) {
        TextRange range = getTextRange();
        return range == null ? defaultRange : range;
    }

    @Override
    public Set<DQLDataType> getDataType() {
        if (values.size() == 1) {
            if (expression instanceof BaseTypedElement entity) {
                return entity.getDataType();
            }
        } else {
            return DQLUtil.calculateFieldType(getExpressions().toArray(new PsiElement[0]));
        }
        return Set.of(DQLDataType.UNKNOWN);
    }

    @Override
    public boolean accessesData() {
        for (DQLExpression value : getExpressions()) {
            if (value instanceof BaseTypedElement entity && entity.accessesData()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(expression).getFieldName();
    }

    public List<DQLExpression> getExpressions() {
        if (!values.isEmpty()) {
            return values;
        } else {
            return List.of(expression);
        }
    }

    public boolean isNamed() {
        return expression != null && expression instanceof DQLParameterExpression;
    }

    public boolean isMissingName() {
        // if it's an optional, unnamed parameter
        return !definition.required && !isNamed() && definition.canBeNamed() && getExpressions().size() == 1
                &&
                // if the parameter is an array, and the parameter expects a list, then it is a correct parameter
                !(expression instanceof DQLBracketExpression && DQLDataType.LIST_OF_EXPRESSIONS.satisfies(definition.getDQLTypes()));
    }

    public List<DQLExpression> getValues() {
        return values;
    }

    public DQLExpression getExpression() {
        return expression;
    }

    public DQLParameterDefinition getDefinition() {
        return definition;
    }

    public PsiElement getNameIdentifier() {
        return values.getFirst() instanceof DQLParameterExpression parameter ? parameter.getParameterName() : expression;
    }

    public @Nullable String getName() {
        if (definition != null) {
            return definition.name;
        }
        if (expression instanceof DQLParameterExpression parameter) {
            return parameter.getName();
        }
        return null;
    }
}

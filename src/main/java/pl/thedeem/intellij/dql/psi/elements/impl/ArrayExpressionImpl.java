package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.ArrayExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ArrayExpressionImpl extends TwoSidesExpressionImpl implements ArrayExpression {
    private CachedValue<Set<DQLDataType>> dataType;

    public ArrayExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Set<DQLDataType> getDataType() {
        if (dataType == null) {
            dataType = CachedValuesManager.getManager(getProject()).createCachedValue(
                () -> new CachedValueProvider.Result<>(recalculateDataType(), this),
                false
            );
        }
        return dataType.getValue();
    }

    @Override
    public boolean accessesData() {
        if (getLeftExpression() instanceof BaseTypedElement entity) {
            return entity.accessesData();
        }
        return false;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("presentation.arrayExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getLeftExpression())
                .addPart("[")
                .addPart(getRightExpression())
                .addPart("]")
                .getFieldName();
    }

    @Override
    public @Nullable DQLExpression getRightExpression() {
        List<DQLExpression> expressions = getExpressions();
        return expressions.size() < 2 ? null : expressions.getLast();
    }

    private Set<DQLDataType> recalculateDataType() {
        Set<DQLDataType> possibleValues = new HashSet<>();
        DQLExpression right = getRightExpression();
        if (right == null) {
            DQLExpression left = getLeftExpression();
            possibleValues.add(DQLDataType.ARRAY);
            possibleValues.add(DQLDataType.ITERATIVE_EXPRESSION);
            if (left instanceof BaseTypedElement element) {
                possibleValues.addAll(element.getDataType());
            }
        }
        possibleValues.add(DQLDataType.ANY);

        return new HashSet<>(possibleValues);
    }
}


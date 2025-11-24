package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.NegativeValueExpression;

import java.util.HashSet;
import java.util.Set;

public abstract class NegativeValueExpressionImpl extends ASTWrapperPsiElement implements NegativeValueExpression {
    private static final Set<DQLDataType> DEFAULT_TYPES = Set.of(DQLDataType.NEGATIVE_DURATION, DQLDataType.NEGATIVE_LONG, DQLDataType.NEGATIVE_DOUBLE);

    public NegativeValueExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public Set<DQLDataType> getDataType() {
        DQLExpression value = getExpressionValue();
        if (value instanceof BaseTypedElement element) {
            Set<DQLDataType> dataType = new HashSet<>(element.getDataType());
            if (dataType.remove(DQLDataType.POSITIVE_DURATION) || dataType.remove(DQLDataType.DURATION)) {
                return Set.of(DQLDataType.NEGATIVE_DURATION);
            }
            if (dataType.remove(DQLDataType.POSITIVE_LONG) || dataType.remove(DQLDataType.LONG)) {
                return Set.of(DQLDataType.NEGATIVE_LONG);
            }
            if (dataType.remove(DQLDataType.POSITIVE_DOUBLE) || dataType.remove(DQLDataType.DOUBLE)) {
                return Set.of(DQLDataType.NEGATIVE_DOUBLE);
            }
            if (dataType.contains(DQLDataType.ITERATIVE_EXPRESSION)) {
                dataType.addAll(DEFAULT_TYPES);
                return dataType;
            }
        }
        // there cannot be any other negative types
        return DEFAULT_TYPES;
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(getName()).getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(this.getName(), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public boolean accessesData() {
        return getExpressionValue() instanceof BaseTypedElement element && element.accessesData();
    }

    private DQLExpression getExpressionValue() {
        return findChildByClass(DQLExpression.class);
    }
}

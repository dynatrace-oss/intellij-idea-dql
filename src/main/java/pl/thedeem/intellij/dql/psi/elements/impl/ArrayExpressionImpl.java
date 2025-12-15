package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.ArrayExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public abstract class ArrayExpressionImpl extends TwoSidesExpressionImpl implements ArrayExpression {
    public ArrayExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    // As we do not know fields values, returning field[anything] is not known for us.
    public @NotNull Collection<String> getDataType() {
        return Set.of();
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
        return new StandardItemPresentation(DQLBundle.message("presentation.arrayExpression"), this, DQLIcon.DQL_EXPRESSION);
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
}


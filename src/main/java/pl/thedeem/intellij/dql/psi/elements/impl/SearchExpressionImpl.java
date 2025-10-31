package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.SearchExpression;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;


import java.util.Set;

public abstract class SearchExpressionImpl extends TwoSidesExpressionImpl implements SearchExpression {
    public SearchExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.BOOLEAN);
    }

    @Override
    public ItemPresentation getPresentation() {
        DQLExpression rightExpression = getRightExpression();
        String text = rightExpression != null ? rightExpression.getText() : getText();
        return new DQLItemPresentation(DQLBundle.message("presentation.searchExpression", getFieldName(), text), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getLeftExpression())
                .getFieldName();
    }

    @Override
    public boolean accessesData() {
        DQLExpression leftExpression = getLeftExpression();
        if (leftExpression instanceof BaseTypedElement element) {
            return element.accessesData();
        }
        return true;
    }
}

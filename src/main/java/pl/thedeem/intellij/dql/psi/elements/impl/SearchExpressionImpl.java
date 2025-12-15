package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.SearchExpression;

public abstract class SearchExpressionImpl extends AbstractOperatorElementImpl implements SearchExpression {
    public SearchExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        DQLExpression rightExpression = getRightExpression();
        String text = rightExpression != null ? rightExpression.getText() : getText();
        return new StandardItemPresentation(DQLBundle.message("presentation.searchExpression", getFieldName(), text), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getLeftExpression())
                .getFieldName();
    }
}

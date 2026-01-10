package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.UnaryExpression;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

public abstract class UnaryExpressionImpl extends AbstractOperatorElementImpl implements UnaryExpression {
    public UnaryExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance().calculateFieldName(
                "not ",
                getLeftExpression()
        );
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.negationExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    protected String getOperationId() {
        return "dql.operator.not";
    }
}

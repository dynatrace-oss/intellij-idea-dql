package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.EqualityExpression;

public abstract class EqualityExpressionImpl extends AbstractOperatorElementImpl implements EqualityExpression {
    public EqualityExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.equalityExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    protected String getOperationId() {
        String symbol = getOperatorSymbol();
        return switch (symbol) {
            case "!=", "<>" -> "dql.operator.notEquals";
            case "==" -> "dql.operator.equals";
            case null, default -> symbol;
        };
    }
}

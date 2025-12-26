package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.ConditionExpression;

public abstract class ConditionExpressionImpl extends AbstractOperatorElementImpl implements ConditionExpression {
    public ConditionExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.conditionExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    protected String getOperationId() {
        String symbol = getOperatorSymbol();
        return switch (symbol) {
            case "and" -> "dql.operator.and";
            case "or" -> "dql.operator.or";
            case "xor" -> "dql.operator.xor";
            case null, default -> symbol;
        };
    }
}

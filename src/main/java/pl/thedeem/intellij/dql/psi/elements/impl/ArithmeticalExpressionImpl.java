package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.ArithmeticalExpression;

public abstract class ArithmeticalExpressionImpl extends AbstractOperatorElementImpl implements ArithmeticalExpression {
    public ArithmeticalExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    protected String getOperationId() {
        String symbol = getOperatorSymbol();
        return switch (symbol) {
            case "-" -> "dql.operator.subtract";
            case "+" -> "dql.operator.add";
            case "/" -> "dql.operator.divide";
            case "%" -> "dql.operator.modulo";
            case "*" -> "dql.operator.multiply";
            case null, default -> symbol;
        };
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.arithmeticalExpression"), this, DQLIcon.DQL_EXPRESSION);
    }
}

package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.ComparisonExpression;

public abstract class ComparisonExpressionImpl extends AbstractOperatorElementImpl implements ComparisonExpression {
    public ComparisonExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.comparisonExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    protected String getOperationId() {
        String symbol = getOperatorSymbol();
        return switch (symbol) {
            case ">" -> "dql.operator.greater";
            case ">=" -> "dql.operator.greaterEquals";
            case "<" -> "dql.operator.lower";
            case "<=" -> "dql.operator.lowerEquals";
            case null, default -> symbol;
        };
    }
}

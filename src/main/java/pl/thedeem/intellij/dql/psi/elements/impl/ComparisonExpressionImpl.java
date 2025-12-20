package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLComparisonOperator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.ComparisonExpression;

import java.util.List;

public abstract class ComparisonExpressionImpl extends AbstractOperatorElementImpl implements ComparisonExpression {
    public ComparisonExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.comparisonExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        List<DQLExpression> expressions = PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
        DQLComparisonOperator operator = PsiTreeUtil.getChildOfType(this, DQLComparisonOperator.class);
        return new DQLFieldNamesGenerator()
                .addPart(expressions, operator)
                .getFieldName();
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

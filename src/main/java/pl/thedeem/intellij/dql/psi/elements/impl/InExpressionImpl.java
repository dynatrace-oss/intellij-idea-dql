package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.InExpression;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

public abstract class InExpressionImpl extends AbstractOperatorElementImpl implements InExpression {

    public InExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.inExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance().calculateFieldName(
                getLeftExpression(),
                " in ",
                getRightExpression());
    }

    @Override
    protected String getOperationId() {
        return "dql.operator.in";
    }
}

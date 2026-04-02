package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.ArrayExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.services.query.DQLFieldsCalculatorService;

public abstract class ArrayExpressionImpl extends AbstractOperatorElementImpl implements ArrayExpression {
    public ArrayExpressionImpl(@NotNull ASTNode node) {
        super(node);
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
        return DQLFieldsCalculatorService.getInstance().calculateFieldName(
                getLeftExpression(),
                "[",
                getRightExpression(),
                "]"
        );
    }

    @Override
    protected String getOperationId() {
        return "dql.operator.array";
    }

    @Override
    public @Nullable PsiElement getBaseExpression() {
        PsiElement leftSide = getLeftExpression();
        while (leftSide instanceof ArrayExpression arrayExpression) {
            leftSide = arrayExpression.getLeftExpression();
        }
        return leftSide;
    }
}


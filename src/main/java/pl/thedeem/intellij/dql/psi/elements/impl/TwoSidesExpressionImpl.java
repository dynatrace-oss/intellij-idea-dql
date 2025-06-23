package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.elements.TwoSidesExpression;

import java.util.List;

public abstract class TwoSidesExpressionImpl extends ASTWrapperPsiElement implements TwoSidesExpression {

    public TwoSidesExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Unmodifiable @NotNull List<DQLExpression> getExpressions() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
    }

    @Override
    public @Nullable ExpressionOperatorImpl getOperator() {
        return PsiTreeUtil.getChildOfAnyType(this, ExpressionOperatorImpl.class);
    }

    @Override
    public @Nullable DQLExpression getLeftExpression() {
        List<DQLExpression> expressions = getExpressions();
        return expressions.isEmpty() ? null : expressions.getFirst();
    }

    @Override
    public @Nullable DQLExpression getRightExpression() {
        List<DQLExpression> expressions = getExpressions();
        return expressions.isEmpty() ? null : expressions.getLast();
    }
}

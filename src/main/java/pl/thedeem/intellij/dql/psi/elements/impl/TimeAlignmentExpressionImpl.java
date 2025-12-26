package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.DQLTimeAlignmentOperand;
import pl.thedeem.intellij.dql.psi.elements.TimeAlignmentExpression;

public abstract class TimeAlignmentExpressionImpl extends AbstractOperatorElementImpl implements TimeAlignmentExpression {
    public TimeAlignmentExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.timeAlignmentExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public @Nullable PsiElement getRightExpression() {
        return findChildByClass(DQLTimeAlignmentOperand.class);
    }

    @Override
    protected String getOperationId() {
        return "dql.operator.timeAlignmentAt";
    }
}

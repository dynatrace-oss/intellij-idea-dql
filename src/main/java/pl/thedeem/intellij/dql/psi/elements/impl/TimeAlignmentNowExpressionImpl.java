package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.psi.elements.TimeAlignmentExpression;
import pl.thedeem.intellij.dql.services.query.DQLFieldNamesService;

public abstract class TimeAlignmentNowExpressionImpl extends TimeAlignmentExpressionImpl implements TimeAlignmentExpression {
    public TimeAlignmentNowExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getFieldName() {
        return DQLFieldNamesService.getInstance().calculateFieldName(
                "@",
                getRightExpression()
        );
    }

    @Override
    public @Nullable PsiElement getLeftExpression() {
        return null;
    }
}

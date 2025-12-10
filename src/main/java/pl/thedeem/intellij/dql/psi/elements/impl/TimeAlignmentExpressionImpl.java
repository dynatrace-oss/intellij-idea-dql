package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLTimeAlignmentOperand;
import pl.thedeem.intellij.dql.psi.elements.TimeAlignmentExpression;

public abstract class TimeAlignmentExpressionImpl extends AbstractOperatorElementImpl implements TimeAlignmentExpression {
    public TimeAlignmentExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getLeftExpression())
                .addPart("@")
                .addPart(getRightExpression())
                .getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.timeAlignmentExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public @Nullable DQLExpression getRightExpression() {
        return getDurationElement() instanceof DQLExpression expr ? expr : null;
    }

    @Override
    public @Nullable PsiElement getDurationElement() {
        return findChildByClass(DQLTimeAlignmentOperand.class);
    }
}

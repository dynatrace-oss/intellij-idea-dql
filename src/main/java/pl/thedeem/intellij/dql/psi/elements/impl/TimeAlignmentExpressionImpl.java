package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.DQLTimeAlignmentOperand;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.TimeAlignmentExpression;

import java.util.Set;

public abstract class TimeAlignmentExpressionImpl extends TwoSidesExpressionImpl implements TimeAlignmentExpression {
    public TimeAlignmentExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.TIMESTAMP);
    }

    @Override
    public boolean accessesData() {
        for (PsiElement param : getChildren()) {
            if (param instanceof BaseTypedElement entity && entity.accessesData()) {
                return true;
            }
        }
        return false;
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
        return new DQLItemPresentation(DQLBundle.message("presentation.timeAlignmentExpression"), this, DQLIcon.DQL_EXPRESSION);
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

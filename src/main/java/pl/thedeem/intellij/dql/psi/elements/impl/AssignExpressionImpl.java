package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.elements.AssignExpression;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;

import java.util.Collection;
import java.util.Set;

public abstract class AssignExpressionImpl extends AbstractOperatorElementImpl implements AssignExpression {
    public AssignExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull Collection<String> getDataType() {
        return getRightExpression() instanceof BaseTypedElement el ? el.getDataType() : Set.of();
    }

    @Override
    public ItemPresentation getPresentation() {
        PsiElement leftExpression = getLeftExpression();
        String text = leftExpression != null ? leftExpression.getText() : getText();
        return new StandardItemPresentation(DQLBundle.message("presentation.assignExpression", text), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getLeftExpression())
                .getFieldName();
    }

    @Override
    public boolean accessesData() {
        PsiElement rightExpression = getRightExpression();
        if (rightExpression instanceof BaseTypedElement element) {
            return element.accessesData();
        }
        return true;
    }

    @Override
    protected String getOperationId() {
        return "dql.operator.assign";
    }
}

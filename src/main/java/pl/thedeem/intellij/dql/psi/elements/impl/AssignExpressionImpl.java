package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.DQLSubqueryExpression;
import pl.thedeem.intellij.dql.psi.elements.AssignExpression;

import java.util.HashSet;
import java.util.Set;

public abstract class AssignExpressionImpl extends TwoSidesExpressionImpl implements AssignExpression {
    public AssignExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Set<DQLDataType> getDataType() {
        Set<DQLDataType> result = new HashSet<>(Set.of(DQLDataType.ASSIGN_EXPRESSION, DQLDataType.RECORD));
        PsiElement value = getLastChild();
        if (value instanceof DQLSubqueryExpression) {
            result.add(DQLDataType.NAMED_SUBQUERY_EXPRESSION);
        }
        return result;
    }

    @Override
    public ItemPresentation getPresentation() {
        DQLExpression leftExpression = getLeftExpression();
        String text = leftExpression != null ? leftExpression.getText() : getText();
        return new DQLItemPresentation(DQLBundle.message("presentation.assignExpression", text), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getLeftExpression())
                .getFieldName();
    }

    @Override
    public boolean accessesData() {
        DQLExpression rightExpression = getRightExpression();
        if (rightExpression instanceof BaseTypedElement element) {
            return element.accessesData();
        }
        return true;
    }
}

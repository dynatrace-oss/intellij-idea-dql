package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLConditionOperator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.ConditionExpression;

import java.util.List;
import java.util.Set;

public abstract class ConditionExpressionImpl extends TwoSidesExpressionImpl implements ConditionExpression {
    public ConditionExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.BOOLEAN);
    }

    @Override
    public boolean accessesData() {
        PsiElement left = this.getFirstChild();
        if (left instanceof BaseTypedElement entity && entity.accessesData()) {
            return true;
        }
        PsiElement right = this.getFirstChild();
        return right instanceof BaseTypedElement entity && entity.accessesData();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("presentation.conditionExpression"), this, DQLIcon.DQL_EXPRESSION);
    }

    @Override
    public String getFieldName() {
        List<DQLExpression> expressions = PsiTreeUtil.getChildrenOfTypeAsList(this, DQLExpression.class);
        DQLConditionOperator operator = PsiTreeUtil.getChildOfType(this, DQLConditionOperator.class);
        return new DQLFieldNamesGenerator()
                .addPart(expressions, operator)
                .getFieldName();
    }
}

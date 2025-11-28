package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.psi.DQLParameterName;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.ParameterExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class ParameterExpressionImpl extends ASTWrapperPsiElement implements ParameterExpression {
    public ParameterExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        DQLParameterName parameter = this.findChildByClass(DQLParameterName.class);
        return parameter != null ? parameter.getName() : getText();
    }

    @Override
    public Set<DQLDataType> getDataType() {
        DQLExpression expression = getExpression();
        return expression instanceof BaseTypedElement entity ? entity.getDataType() : Set.of(DQLDataType.ANY);
    }

    @Override
    public boolean accessesData() {
        DQLExpression expression = getExpression();
        return expression instanceof BaseTypedElement entity && entity.accessesData();
    }

    private DQLExpression getExpression() {
        return findChildByClass(DQLExpression.class);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getName())
                .addPart(": ")
                .addPart(getExpression())
                .getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DQLBundle.message("presentation.parameterExpression", getName()), this, DQLIcon.DQL_STATEMENT_PARAMETER);
    }
}

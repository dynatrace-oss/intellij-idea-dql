package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseTypedElement;
import pl.thedeem.intellij.dql.psi.elements.UnaryExpression;

import java.util.Set;

public abstract class UnaryExpressionImpl extends ASTWrapperPsiElement implements UnaryExpression {
    public UnaryExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.BOOLEAN);
    }

    @Override
    public boolean accessesData() {
        PsiElement lastChild = getLastChild();
        return lastChild instanceof BaseTypedElement entity && entity.accessesData();
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart("not")
                .addPart(findChildByClass(DQLExpression.class))
                .getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("presentation.negationExpression"), this, DQLIcon.DQL_EXPRESSION);
    }
}

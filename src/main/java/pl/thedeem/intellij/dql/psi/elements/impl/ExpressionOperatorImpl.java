package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.tree.IElementType;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseNamedElement;
import org.jetbrains.annotations.NotNull;

public abstract class ExpressionOperatorImpl extends ASTWrapperPsiElement implements BaseNamedElement {
    public ExpressionOperatorImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(DQLBundle.message("expression.operator." + getNode().getElementType().toString().toLowerCase()), this, DQLIcon.DQL_OPERATOR);
    }

    @Override
    public @NotNull String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getText())
                .getFieldName();
    }

    public IElementType getNodeType() {
        return getFirstChild().getNode().getElementType();
    }

    @Override
    public boolean accessesData() {
        return false;
    }
}

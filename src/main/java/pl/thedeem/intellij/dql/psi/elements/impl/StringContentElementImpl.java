package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BaseNamedElement;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class StringContentElementImpl extends ASTWrapperPsiElement implements BaseNamedElement {
    public StringContentElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText();
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(getText()).getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(this.getName(), this, DQLIcon.DQL_STRING);
    }

    @Override
    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.STRING);
    }

    @Override
    public boolean accessesData() {
        return false;
    }
}

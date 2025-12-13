package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.elements.BaseNamedElement;

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
        return new StandardItemPresentation(this.getName(), this, DQLIcon.DQL_STRING);
    }

    @Override
    public @NotNull Set<String> getDataType() {
        return Set.of("dql.dataType.string");
    }

    @Override
    public boolean accessesData() {
        return false;
    }
}

package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.NullElement;

import java.util.Set;

public abstract class NullElementImpl extends ASTWrapperPsiElement implements NullElement {
    public NullElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull String getName() {
        return getText().trim().toLowerCase();
    }

    @Override
    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.NULL);
    }

    @Override
    public boolean accessesData() {
        return false;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new DQLItemPresentation(this.getName(), this, DQLIcon.NULL);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getName())
                .getFieldName();
    }
}

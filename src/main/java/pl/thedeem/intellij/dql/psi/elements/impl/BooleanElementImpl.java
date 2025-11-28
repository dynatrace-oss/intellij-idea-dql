package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.BooleanElement;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class BooleanElementImpl extends ASTWrapperPsiElement implements BooleanElement {
    public BooleanElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull String getName() {
        return getText().trim().toLowerCase();
    }

    @Override
    public Set<DQLDataType> getDataType() {
        return Set.of(DQLDataType.BOOLEAN);
    }

    @Override
    public boolean accessesData() {
        return false;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(this.getName(), this, DQLIcon.DQL_BOOLEAN);
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator()
                .addPart(getName())
                .getFieldName();
    }
}

package pl.thedeem.intellij.dql.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.definition.DQLFieldNamesGenerator;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dql.psi.elements.SortKeywordElement;
import org.jetbrains.annotations.NotNull;

public abstract class SortKeywordElementImpl extends ASTWrapperPsiElement implements SortKeywordElement {
    public SortKeywordElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String getName() {
        return getText().trim().toLowerCase();
    }

    @Override
    public String getFieldName() {
        return new DQLFieldNamesGenerator().addPart(getName()).getFieldName();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(this.getName(), this, DQLIcon.DQL_SORT_DIRECTION);
    }

    @Override
    public boolean isWritable() {
        return false;
    }
}

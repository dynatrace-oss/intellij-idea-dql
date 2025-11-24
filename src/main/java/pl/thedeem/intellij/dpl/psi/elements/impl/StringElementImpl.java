package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.impl.DPLStringImpl;
import pl.thedeem.intellij.dpl.psi.elements.StringElement;

public abstract class StringElementImpl extends DPLStringImpl implements StringElement {
    public StringElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull String newName) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.string"), this, DPLIcon.LITERAL);
    }
}

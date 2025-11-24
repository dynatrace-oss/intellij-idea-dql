package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.psi.elements.MatcherNameElement;

public abstract class MatcherNameElementImpl extends ASTWrapperPsiElement implements MatcherNameElement {
    public MatcherNameElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull String var1) throws IncorrectOperationException {
        throw new IncorrectOperationException();//TODO
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.key"), this, DPLIcon.KEY);
    }
}

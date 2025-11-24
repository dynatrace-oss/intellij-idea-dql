package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.psi.elements.QuantifierLimitElement;

public abstract class QuantifierLimitElementImpl extends ASTWrapperPsiElement implements QuantifierLimitElement {
    public QuantifierLimitElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        return getText();
    }

    @Override
    public PsiElement setName(@NotNull String var1) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public @NotNull Long getLongValue() {
        String text = getText();
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(getName(), this, DPLIcon.NUMBER);
    }
}

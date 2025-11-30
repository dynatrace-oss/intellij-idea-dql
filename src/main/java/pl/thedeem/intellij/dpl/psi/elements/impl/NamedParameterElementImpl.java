package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.impl.DPLDefinitionExpressionImpl;
import pl.thedeem.intellij.dpl.psi.DPLParameterName;
import pl.thedeem.intellij.dpl.psi.elements.ParameterElement;

public abstract class NamedParameterElementImpl extends DPLDefinitionExpressionImpl implements ParameterElement, PsiNameIdentifierOwner {
    public NamedParameterElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        PsiElement identifier = getNameIdentifier();
        return identifier instanceof PsiNamedElement el ? el.getName() : null;
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return PsiTreeUtil.getChildOfType(this, DPLParameterName.class);
    }

    @Override
    public PsiElement setName(@NotNull String newName) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.parameter", getName()), this, DPLIcon.CONFIGURATION_PARAMETER);
    }
}

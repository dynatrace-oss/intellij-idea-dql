package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.psi.elements.ConfigurationElement;

public abstract class QuantifierContentElementImpl extends ASTWrapperPsiElement implements ConfigurationElement {
    public QuantifierContentElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.quantifier"), this, DPLIcon.QUANTIFIER);
    }
}

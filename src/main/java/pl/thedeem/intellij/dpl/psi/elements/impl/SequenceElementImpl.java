package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.impl.DPLGroupExpressionImpl;
import pl.thedeem.intellij.dpl.psi.elements.SequenceElement;

public abstract class SequenceElementImpl extends DPLGroupExpressionImpl implements SequenceElement {
    public SequenceElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.sequence"), this, DPLIcon.EXPRESSION);
    }
}

package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.impl.DPLExpressionImpl;
import pl.thedeem.intellij.dpl.psi.elements.CommandMatchersElement;

public abstract class CommandMatchersElementImpl extends DPLExpressionImpl implements CommandMatchersElement {
    public CommandMatchersElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.matchers", getName()), this, DPLIcon.MATCHERS);
    }
}

package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.impl.DPLExpressionImpl;
import pl.thedeem.intellij.dpl.psi.elements.MetaExpressionElement;

public abstract class MetaExpressionElementImpl extends DPLExpressionImpl implements MetaExpressionElement {
    public MetaExpressionElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.meta", getName()), this, DPLIcon.EXPRESSION);
    }

    @Override
    public @Nullable String getExpressionName() {
        return "meta";
    }
}

package pl.thedeem.intellij.dpl.psi.elements.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.impl.DPLSimpleExpressionImpl;
import pl.thedeem.intellij.dpl.psi.elements.VariableUsageExpressionElement;

public abstract class VariableUsageExpressionElementImpl extends DPLSimpleExpressionImpl implements VariableUsageExpressionElement {
    public VariableUsageExpressionElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable String getName() {
        return getText();
    }

    @Override
    public @Nullable String getExpressionName() {
        return "variable";
    }

    @Override
    public ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.expression"), this, DPLIcon.EXPRESSION);
    }
}

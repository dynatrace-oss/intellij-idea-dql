package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;

public interface ModifierExpression extends BaseExpression {
    @Override
    default @Nullable ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.modifierExpression"), this, DPLIcon.EXPRESSION);
    }
}

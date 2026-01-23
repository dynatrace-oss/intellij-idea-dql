package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;

public interface BaseExpression extends PsiElement, NavigationItem {
    default @Nullable String getExpressionName() {
        return null;
    }

    @Override
    default @Nullable ItemPresentation getPresentation() {
        return new StandardItemPresentation(DPLBundle.message("presentation.expression"), this, DPLIcon.EXPRESSION);
    }

    default @Nullable ExpressionDescription getDefinition() {
        return null;
    }
}

package pl.thedeem.intellij.dql.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DQLItemPresentation implements ItemPresentation {

    private final String name;
    private final PsiElement element;
    private final Icon icon;

    public DQLItemPresentation(String name, Icon icon) {
        this.name = name;
        this.element = null;
        this.icon = icon;
    }

    public DQLItemPresentation(String name, PsiElement element, Icon icon) {
        this.name = name;
        this.element = element;
        this.icon = icon;
    }

    @Override
    public @Nullable String getPresentableText() {
        return name;
    }

    @Override
    public @Nullable String getLocationString() {
        return element != null ? element.getContainingFile().getName() : null;
    }

    @Override
    public @Nullable Icon getIcon(boolean b) {
        return icon;
    }
}

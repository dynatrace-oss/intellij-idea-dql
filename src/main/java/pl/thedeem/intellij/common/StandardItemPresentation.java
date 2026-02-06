package pl.thedeem.intellij.common;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class StandardItemPresentation implements ItemPresentation {
    private final String name;
    private final PsiElement element;
    private final Icon icon;

    public StandardItemPresentation(@NotNull String name, @Nullable Icon icon) {
        this(name, null, icon);
    }

    public StandardItemPresentation(@NotNull String name, @Nullable PsiElement element, @Nullable Icon icon) {
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

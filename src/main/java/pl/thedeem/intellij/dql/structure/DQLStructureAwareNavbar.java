package pl.thedeem.intellij.dql.structure;

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.impl.PsiElementBase;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DQLStructureAwareNavbar extends StructureAwareNavBarModelExtension {
    @NotNull
    @Override
    protected Language getLanguage() {
        return DynatraceQueryLanguage.INSTANCE;
    }

    @Override
    public @Nullable String getPresentableText(Object object) {
        if (object instanceof PsiElementBase namedElement) {
            ItemPresentation presentation = namedElement.getPresentation();
            return presentation != null ? presentation.getPresentableText() : null;
        }
        return null;
    }

    @Override
    @Nullable
    public Icon getIcon(Object object) {
        if (object instanceof PsiElementBase namedElement) {
            ItemPresentation presentation = namedElement.getPresentation();
            return presentation != null ? presentation.getIcon(false) : null;
        }
        return null;
    }
}

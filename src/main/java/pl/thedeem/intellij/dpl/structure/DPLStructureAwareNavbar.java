package pl.thedeem.intellij.dpl.structure;

import com.intellij.ide.navigationToolbar.StructureAwareNavBarModelExtension;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.impl.PsiElementBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;

import javax.swing.*;

public class DPLStructureAwareNavbar extends StructureAwareNavBarModelExtension {
    @NotNull
    @Override
    protected Language getLanguage() {
        return DynatracePatternLanguage.INSTANCE;
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

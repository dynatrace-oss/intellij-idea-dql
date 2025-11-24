package pl.thedeem.intellij.dpl.psi.elements;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

public interface FieldNameElement extends PsiNameIdentifierOwner {
    @NotNull String getExportName();
}

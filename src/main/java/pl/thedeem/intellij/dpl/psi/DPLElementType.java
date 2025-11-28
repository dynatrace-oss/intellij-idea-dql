package pl.thedeem.intellij.dpl.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;

public class DPLElementType extends IElementType {
    public DPLElementType(@NotNull @NonNls String debugName) {
        super(debugName, DynatracePatternLanguage.INSTANCE);
    }
}

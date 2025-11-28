package pl.thedeem.intellij.dpl.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;

public class DPLTokenType extends IElementType {
    public DPLTokenType(@NonNls @NotNull String debugName) {
        super(debugName, DynatracePatternLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "DPLTokenType." + super.toString();
    }
}

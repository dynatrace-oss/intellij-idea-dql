package pl.thedeem.intellij.dql.psi;

import com.intellij.psi.tree.IElementType;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DQLTokenType extends IElementType {
    public DQLTokenType(@NonNls @NotNull String debugName) {
        super(debugName, DynatraceQueryLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "DQLTokenType." + super.toString();
    }
}

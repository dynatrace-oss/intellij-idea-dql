package pl.thedeem.intellij.dql.psi;

import com.intellij.psi.tree.IElementType;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DQLElementType extends IElementType {
    public DQLElementType(@NotNull @NonNls String debugName) {
        super(debugName, DynatraceQueryLanguage.INSTANCE);
    }
}

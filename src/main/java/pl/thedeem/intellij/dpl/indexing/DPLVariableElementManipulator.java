package pl.thedeem.intellij.dpl.indexing;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

public class DPLVariableElementManipulator extends AbstractElementManipulator<DPLVariable> {
    @Override
    public @Nullable DPLVariable handleContentChange(@NotNull DPLVariable variable, @NotNull TextRange textRange, String newString) throws IncorrectOperationException {
        return (DPLVariable) variable.setName(newString);
    }
}

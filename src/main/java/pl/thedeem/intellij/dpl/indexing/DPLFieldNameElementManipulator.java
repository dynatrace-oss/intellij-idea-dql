package pl.thedeem.intellij.dpl.indexing;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;

public class DPLFieldNameElementManipulator extends AbstractElementManipulator<DPLFieldName> {
    @Override
    public @Nullable DPLFieldName handleContentChange(@NotNull DPLFieldName field, @NotNull TextRange textRange, String newString) throws IncorrectOperationException {
        return (DPLFieldName) field.setName(newString);
    }
}

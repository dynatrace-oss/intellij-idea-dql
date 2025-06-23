package pl.thedeem.intellij.dql.indexing;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DQLVariableElementManipulator extends AbstractElementManipulator<DQLVariableExpression> {
    @Override
    public @Nullable DQLVariableExpression handleContentChange(@NotNull DQLVariableExpression variable, @NotNull TextRange textRange, String newString) throws IncorrectOperationException {
        return (DQLVariableExpression) variable.setName(newString);
    }
}

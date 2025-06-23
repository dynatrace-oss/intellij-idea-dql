package pl.thedeem.intellij.dql.indexing;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import pl.thedeem.intellij.dql.psi.DQLElementFactory;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DQLFieldElementManipulator extends AbstractElementManipulator<DQLFieldExpression> {
    @Override
    public @Nullable DQLFieldExpression handleContentChange(@NotNull DQLFieldExpression field, @NotNull TextRange textRange, String newString) throws IncorrectOperationException {
        PsiElement nameIdentifier = field.getNameIdentifier();
        if (nameIdentifier != null) {
            DQLFieldExpression fieldElement = DQLElementFactory.createFieldElement(newString, field.getProject());
            nameIdentifier.replace(Objects.requireNonNull(fieldElement.getNameIdentifier()));
        }
        return field;
    }
}

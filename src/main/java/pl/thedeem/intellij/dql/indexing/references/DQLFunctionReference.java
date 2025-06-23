package pl.thedeem.intellij.dql.indexing.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class DQLFunctionReference extends PsiReferenceBase<DQLFunctionName> {
    public DQLFunctionReference(@NotNull DQLFunctionName element) {
        super(element, TextRange.from(0, element.getTextLength()));
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    @Override
    public Object @NotNull [] getVariants() {
        return new Object[0];
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        boolean isReference = super.isReferenceTo(element);
        if (!isReference) {
            if (element instanceof DQLFunctionName func) {
                return Objects.equals(myElement.getName(), func.getName());
            }
            if (element instanceof DQLFunctionCallExpression func) {
                return Objects.equals(myElement.getName(), func.getName());
            }
            return false;
        }
        return true;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }
}

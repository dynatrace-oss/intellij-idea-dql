package pl.thedeem.intellij.dql.indexing.references;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLQueryStatementKeyword;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class DQLStatementKeywordReference extends PsiReferenceBase<DQLQueryStatementKeyword> {
    public DQLStatementKeywordReference(@NotNull DQLQueryStatementKeyword element) {
        super(element, TextRange.from(0, element.getTextLength()));
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    // We do not want to have code completions - it's handled separately
    @Override
    public Object @NotNull [] getVariants() {
        return new LookupElement[0];
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        boolean isReference = super.isReferenceTo(element);
        if (!isReference) {
            if (element instanceof DQLQueryStatementKeyword keyword) {
                return Objects.equals(myElement.getName(), keyword.getName());
            }
            if (element instanceof DQLQueryStatement statement) {
                return Objects.equals(myElement.getName(), statement.getName());
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

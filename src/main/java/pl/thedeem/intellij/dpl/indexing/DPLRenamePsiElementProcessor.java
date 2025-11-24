package pl.thedeem.intellij.dpl.indexing;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Pass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

import java.util.Map;

public class DPLRenamePsiElementProcessor extends RenamePsiElementProcessor {
    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof DPLFieldName || element instanceof DPLVariable;
    }

    @Override
    public void substituteElementToRename(@NotNull PsiElement element, @NotNull Editor editor, @NotNull Pass<? super PsiElement> renameCallback) {
        super.substituteElementToRename(element, editor, renameCallback);
    }

    @Override
    public void prepareRenaming(@NotNull PsiElement element, @NotNull String newName, @NotNull Map<PsiElement, String> allRenames, @NotNull SearchScope scope) {
        super.prepareRenaming(element, newName, allRenames, scope);
    }
}

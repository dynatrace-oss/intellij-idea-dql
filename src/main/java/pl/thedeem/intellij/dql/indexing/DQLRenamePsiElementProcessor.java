package pl.thedeem.intellij.dql.indexing;

import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DQLRenamePsiElementProcessor extends RenamePsiElementProcessor {
    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof DQLFieldExpression || element instanceof DQLVariableExpression;
    }

    @Override
    public void prepareRenaming(@NotNull PsiElement element, @NotNull String newName, @NotNull Map<PsiElement, String> allRenames) {
        super.prepareRenaming(element, newName, allRenames);
    }
}

package pl.thedeem.intellij.dql.indexing;

import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.psi.DQLFieldExpression;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DQLRefactoringSupportProvider extends RefactoringSupportProvider {
  @Override
  public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement elementToRename, @Nullable PsiElement context) {
    return (elementToRename instanceof DQLFieldExpression) || (elementToRename instanceof DQLVariableExpression);
  }
}

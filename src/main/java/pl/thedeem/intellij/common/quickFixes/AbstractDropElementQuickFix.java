package pl.thedeem.intellij.common.quickFixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractDropElementQuickFix implements LocalQuickFix {
    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
        if (document == null) {
            return;
        }
        PsiElement toRemove = getElementToRemove(element);
        deleteElement(toRemove, document);
    }

    protected void deleteElement(@NotNull PsiElement element, @NotNull Document document) {
        int start = element.getTextRange().getStartOffset();
        int end = element.getTextRange().getEndOffset();
        document.deleteString(start, end);
    }

    protected @NotNull PsiElement getElementToRemove(@NotNull PsiElement original) {
        return original;
    }
}

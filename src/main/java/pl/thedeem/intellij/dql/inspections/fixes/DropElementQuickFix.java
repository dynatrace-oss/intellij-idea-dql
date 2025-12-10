package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLTypes;

public class DropElementQuickFix implements LocalQuickFix {
    @SafeFieldForPreview
    private final IElementType separator;

    public DropElementQuickFix() {
        this.separator = DQLTypes.COMMA;
    }

    public DropElementQuickFix(@NotNull IElementType separator) {
        this.separator = separator;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
        PsiElement element = descriptor.getPsiElement();
        Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
        if (document == null) {
            return;
        }
        int start = element.getTextRange().getStartOffset();
        int end = element.getTextRange().getEndOffset();

        PsiElement previousElement = PsiUtils.getPreviousElement(element);
        if (previousElement != null && previousElement.getNode().getElementType() == this.separator) {
            start = previousElement.getTextRange().getStartOffset();
        } else {
            PsiElement nextElement = PsiUtils.getNextElement(element);
            if (nextElement != null && nextElement.getNode().getElementType() == this.separator) {
                end = nextElement.getTextRange().getEndOffset();
            }
        }
        document.deleteString(start, end);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.dropElement");
    }
}

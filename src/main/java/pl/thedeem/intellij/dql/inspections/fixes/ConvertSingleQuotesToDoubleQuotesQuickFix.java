package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;

public class ConvertSingleQuotesToDoubleQuotesQuickFix implements LocalQuickFix {
    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
        PsiElement element = descriptor.getPsiElement();

        Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
        if (document == null) {
            return;
        }

        document.replaceString(element.getTextRange().getStartOffset(), element.getTextRange().getStartOffset() + 1, "\"");
        document.replaceString(element.getTextRange().getEndOffset() - 1, element.getTextRange().getEndOffset(), "\"");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.singleQuotes.fix.convertToDouble");
    }
}

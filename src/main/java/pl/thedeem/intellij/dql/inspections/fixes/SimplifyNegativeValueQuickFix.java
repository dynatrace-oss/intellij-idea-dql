package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLNegativeValueExpression;

public class SimplifyNegativeValueQuickFix implements LocalQuickFix {
    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
        PsiElement element = descriptor.getPsiElement();
        PsiFile psiFile = element.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            return;
        }
        int amount = 0;
        if (element instanceof DQLNegativeValueExpression negativeValue) {
            DQLNegativeValueExpression bottom = negativeValue;
            while (bottom.getExpression() instanceof DQLNegativeValueExpression child) {
                bottom = child;
                amount++;
            }
        }
        if (amount > 0) {
            int start = element.getTextRange().getStartOffset();
            int end = start + amount + (amount % 2 == 1 ? 1 : 0);
            document.deleteString(start, end);
        }
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.simplifyNegativeValue");
    }
}

package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;

import java.util.List;

public class AddBracketsToParameterValueQuickFix implements LocalQuickFix {
    @SafeFieldForPreview
    private final List<SmartPsiElementPointer<DQLExpression>> parameters;

    public AddBracketsToParameterValueQuickFix(@NotNull List<DQLExpression> parameters) {
        this.parameters = parameters.stream()
                .map(SmartPointerManager::createPointer)
                .toList();
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
        PsiElement element = descriptor.getPsiElement();
        PsiFile psiFile = element.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            return;
        }
        DQLExpression first = parameters.getFirst().getElement();
        DQLExpression last = parameters.getLast().getElement();
        if (first == null || last == null) {
            return;
        }
        TextRange textRange = new TextRange(first.getTextRange().getStartOffset(), last.getTextRange().getEndOffset());
        int start = textRange.getStartOffset();
        if (first instanceof DQLParameterExpression parameterExpression && parameterExpression.getExpression() != null) {
            start = parameterExpression.getExpression().getTextRange().getStartOffset();
        }
        int end = textRange.getEndOffset();
        document.insertString(start, "{");
        document.insertString(end + 1, "}");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.command.advisedBrackets.fix.missingBrackets");
    }
}

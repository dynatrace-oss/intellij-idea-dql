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
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;

public class AddBracketsToParameterValue implements LocalQuickFix {
    @SafeFieldForPreview
    private final DQLParameterObject parameter;

    public AddBracketsToParameterValue(DQLParameterObject parameter) {
        this.parameter = parameter;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
        PsiElement element = descriptor.getPsiElement();
        PsiFile psiFile = element.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        if (document == null) {
            return;
        }
        DQLExpression first = parameter.getValues().getFirst();
        int start = first.getTextRange().getStartOffset();
        if (first instanceof DQLParameterExpression parameterExpression && parameterExpression.getExpression() != null) {
            start = parameterExpression.getExpression().getTextRange().getStartOffset();
        }
        int end = parameter.getValues().getLast().getTextRange().getEndOffset();
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

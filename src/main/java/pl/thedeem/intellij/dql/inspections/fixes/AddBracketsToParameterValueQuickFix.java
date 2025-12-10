package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;

public class AddBracketsToParameterValueQuickFix implements LocalQuickFix {
    @SafeFieldForPreview
    private final MappedParameter parameter;

    public AddBracketsToParameterValueQuickFix(MappedParameter parameter) {
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
        DQLExpression first = parameter.holder();
        TextRange textRange = parameter.getTextRange();
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

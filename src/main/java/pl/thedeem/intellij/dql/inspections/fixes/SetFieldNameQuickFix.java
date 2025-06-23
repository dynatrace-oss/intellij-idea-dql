package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;

public class SetFieldNameQuickFix implements LocalQuickFix {
    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
        PsiElement element = descriptor.getPsiElement();
        PsiFile psiFile = element.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        if (document == null || editor == null) {
            return;
        }

        editor.getCaretModel().moveToOffset(element.getTextRange().getStartOffset());

        TemplateManager templateManager = TemplateManager.getInstance(project);
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        if (element instanceof DQLAssignExpression assignExpression) {
            String initialValue = assignExpression.getFieldName();
            DQLExpression leftExpression = assignExpression.getLeftExpression();
            TextRange textRange = leftExpression != null ? leftExpression.getTextRange() : element.getTextRange();
            document.deleteString(textRange.getStartOffset(), textRange.getEndOffset());
            template.addVariable("fieldName", new TextExpression(initialValue), new EmptyNode(), true);
        }
        else {
            template.addVariable("fieldName", new EmptyNode(), new EmptyNode(), true);
            template.addTextSegment("=");
        }

        templateManager.startTemplate(editor, template);
        AutoPopupController.getInstance(project).autoPopupParameterInfo(editor, psiFile);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.setName");
    }
}

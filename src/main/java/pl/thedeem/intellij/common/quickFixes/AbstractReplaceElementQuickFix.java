package pl.thedeem.intellij.common.quickFixes;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractReplaceElementQuickFix<T extends PsiElement> implements LocalQuickFix {
    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (document == null || editor == null) {
            return;
        }

        T toReplace = getElementToReplace(element);
        if (toReplace == null) {
            return;
        }

        replaceElement(toReplace, document, editor, project);
    }

    protected abstract @Nullable T getElementToReplace(@NotNull PsiElement element);

    protected void replaceElement(@NotNull T element, @NotNull Document document, @NotNull Editor editor, @NotNull Project project) {
        String previousText = getDefaultReplacement(element);
        int startOffset = element.getTextRange().getStartOffset();
        document.deleteString(startOffset, element.getTextRange().getEndOffset());

        InjectedLanguageManager host = InjectedLanguageManager.getInstance(project);
        int hostCaretOffset = host.injectedToHost(element, startOffset);
        PsiFile hostFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(element);
        TemplateManager templateManager = TemplateManager.getInstance(project);
        Template template = prepareTemplate(templateManager, previousText);
        editor.getCaretModel().moveToOffset(hostCaretOffset);
        templateManager.startTemplate(editor, template);
        AutoPopupController.getInstance(project).autoPopupParameterInfo(editor, hostFile);
    }

    protected abstract @NotNull String getDefaultReplacement(@NotNull T element);

    protected Template prepareTemplate(@NotNull TemplateManager templateManager, String previousText) {
        Template template = templateManager.createTemplate("", "");
        template.addVariable("replacement", new TextExpression(previousText), new EmptyNode(), true);
        return template;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}

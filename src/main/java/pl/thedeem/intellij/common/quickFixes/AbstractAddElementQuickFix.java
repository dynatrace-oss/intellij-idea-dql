package pl.thedeem.intellij.common.quickFixes;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
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

public abstract class AbstractAddElementQuickFix implements LocalQuickFix {
    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        PsiFile file = element.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (document == null || editor == null) {
            return;
        }

        int caretPosition = getCaretPosition(element);
        InjectedLanguageManager host = InjectedLanguageManager.getInstance(project);
        int hostCaretOffset = host.injectedToHost(element, caretPosition);
        TemplateManager templateManager = TemplateManager.getInstance(project);
        Template template = prepareTemplate(element, templateManager, document);
        if (template == null) {
            return;
        }
        editor.getCaretModel().moveToOffset(hostCaretOffset);
        templateManager.startTemplate(editor, template);
        AutoPopupController.getInstance(project).autoPopupParameterInfo(editor, file);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    protected abstract int getCaretPosition(@NotNull PsiElement element);

    protected abstract @Nullable Template prepareTemplate(@NotNull PsiElement element, @NotNull TemplateManager templateManager, @NotNull Document document);
}

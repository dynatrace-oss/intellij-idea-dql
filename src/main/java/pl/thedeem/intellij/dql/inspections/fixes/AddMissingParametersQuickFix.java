package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.completion.insertions.InsertionsUtils;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.util.Collection;

public class AddMissingParametersQuickFix implements LocalQuickFix {
    @SafeFieldForPreview
    private final Collection<Parameter> missing;
    private final int offset;
    private final boolean addComa;

    public AddMissingParametersQuickFix(@NotNull Collection<Parameter> missingParameters, int offset, boolean addComa) {
        this.missing = missingParameters;
        this.offset = offset;
        this.addComa = addComa;
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) throws IncorrectOperationException {
        PsiElement element = descriptor.getPsiElement();
        PsiFile psiFile = element.getContainingFile();
        Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        if (document == null || editor == null) {
            return;
        }

        TemplateManager templateManager = TemplateManager.getInstance(project);
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        int i = 0;
        for (Parameter param : missing) {
            if (addComa || i > 0) {
                template.addTextSegment(", ");
            } else {
                template.addTextSegment(" ");
            }
            if (param.requiresName()) {
                template.addTextSegment(param.name() + ": ");
            }
            InsertionsUtils.handleInsertionDefaultValue(param, template);
            i++;
        }
        editor.getCaretModel().moveToOffset(offset);
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
        return DQLBundle.message(missing.size() > 1 ? "inspection.fix.addMissingParameters" : "inspection.fix.addMissingParameter",
                DQLBundle.print(missing.stream().map(Parameter::name).toList()));
    }
}

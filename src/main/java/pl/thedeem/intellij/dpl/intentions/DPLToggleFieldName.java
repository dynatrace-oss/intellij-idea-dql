package pl.thedeem.intellij.dpl.intentions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLTypes;

import javax.swing.*;

public class DPLToggleFieldName extends PsiElementBaseIntentionAction implements Iconable {
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        DPLExpressionDefinition expression = PsiTreeUtil.getParentOfType(element, DPLExpressionDefinition.class);
        if (expression == null) {
            return false;
        }
        if (expression.getExportedName() != null) {
            setText(DPLBundle.message("intention.toggleFieldName.removeName"));
        } else {
            setText(DPLBundle.message("intention.toggleFieldName.addName"));
        }
        return true;
    }

    @Override
    public @NotNull Icon getIcon(int flags) {
        return DPLIcon.INTENTION;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        DPLExpressionDefinition expression = PsiTreeUtil.getParentOfType(element, DPLExpressionDefinition.class);
        if (expression == null) {
            return;
        }

        DPLFieldName existingField = expression.getExportedName();
        Document document = editor.getDocument();
        if (existingField != null) {
            PsiElement colon = existingField.getPrevSibling();
            if (DPLTypes.COLON.equals(colon.getNode().getElementType())) {
                document.deleteString(colon.getTextRange().getStartOffset(), existingField.getTextRange().getEndOffset());
            }
        } else {
            if (!element.getContainingFile().isPhysical() || !element.isValid()) {
                return;
            }

            PsiFile hostFile = InjectedLanguageManager.getInstance(project).getTopLevelFile(expression);
            TemplateManager templateManager = TemplateManager.getInstance(project);
            Template template = templateManager.createTemplate("", "");
            template.setToReformat(true);
            template.addTextSegment(":");
            template.addVariable("fieldName", new TextExpression(""), new EmptyNode(), true);
            editor.getCaretModel().moveToOffset(expression.getTextRange().getEndOffset());
            templateManager.startTemplate(editor, template);
            AutoPopupController.getInstance(project).autoPopupParameterInfo(editor, hostFile);
        }
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DynatracePatternLanguage.DPL_DISPLAY_NAME;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}

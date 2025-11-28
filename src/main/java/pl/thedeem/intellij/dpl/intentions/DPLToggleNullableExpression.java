package pl.thedeem.intellij.dpl.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLNullable;
import pl.thedeem.intellij.dpl.psi.DPLTypes;

import javax.swing.*;

public class DPLToggleNullableExpression extends PsiElementBaseIntentionAction implements Iconable {
    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        DPLExpressionDefinition expression = PsiTreeUtil.getParentOfType(element, DPLExpressionDefinition.class);
        if (expression == null) {
            return false;
        }
        if (expression.getNullable() != null) {
            setText(DPLBundle.message("intention.toggleNullable.removeNullable"));
        } else {
            setText(DPLBundle.message("intention.toggleNullable.makeOptional"));
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

        DPLNullable nullable = expression.getNullable();
        Document document = editor.getDocument();
        if (nullable != null) {
            document.deleteString(nullable.getTextRange().getStartOffset(), nullable.getTextRange().getEndOffset());
        } else {
            DPLFieldName fieldName = expression.getExportedName();
            if (fieldName != null) {
                PsiElement colon = fieldName.getPrevSibling();
                if (DPLTypes.COLON.equals(colon.getNode().getElementType())) {
                    document.insertString(colon.getTextRange().getStartOffset(), "?");
                }
            } else {
                document.insertString(expression.getTextRange().getEndOffset(), "?");
            }
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

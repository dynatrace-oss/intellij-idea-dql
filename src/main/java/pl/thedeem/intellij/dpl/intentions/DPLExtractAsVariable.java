package pl.thedeem.intellij.dpl.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.TokenType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.DPLIcon;
import pl.thedeem.intellij.dpl.DynatracePatternLanguage;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;

import javax.swing.*;
import java.util.Objects;

public class DPLExtractAsVariable extends PsiElementBaseIntentionAction implements Iconable {
    public DPLExtractAsVariable() {
        setText(DPLBundle.message("intention.exportAsMacro"));
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {

        if (editor.getSelectionModel().getSelectionStart() != editor.getSelectionModel().getSelectionEnd()) {
            return true;
        }
        DPLExpressionDefinition expression = PsiTreeUtil.getParentOfType(psiElement, DPLExpressionDefinition.class);
        return expression != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        int selectionStart;
        int selectionEnd;
        String macroDefinition;
        String variableName = "$newVariable";
        Document document = editor.getDocument();
        if (editor.getSelectionModel().getSelectionStart() != editor.getSelectionModel().getSelectionEnd()) {
            selectionStart = getStartOffsetFromSelection(editor.getSelectionModel().getSelectionStart(), element);
            selectionEnd = getEndOffsetFromSelection(editor.getSelectionModel().getSelectionEnd(), element);
            macroDefinition = document.getText(new TextRange(selectionStart, selectionEnd));
        } else {
            DPLExpressionDefinition expression = PsiTreeUtil.getParentOfType(element, DPLExpressionDefinition.class);
            if (expression == null) {
                return;
            }
            selectionStart = expression.getTextRange().getStartOffset();
            selectionEnd = expression.getTextRange().getEndOffset();
            macroDefinition = expression.getText();
        }
        document.replaceString(selectionStart, selectionEnd, variableName);

        document.insertString(0, variableName + " = " + macroDefinition + ";");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DynatracePatternLanguage.DPL_DISPLAY_NAME;
    }

    @Override
    public Icon getIcon(int i) {
        return DPLIcon.INTENTION;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    private int getStartOffsetFromSelection(int selectionStart, @NotNull PsiElement element) {
        PsiElement foundElement = element.getContainingFile().findElementAt(selectionStart);
        if (foundElement == null) {
            return selectionStart;
        }
        DPLExpressionDefinition expression = PsiTreeUtil.getParentOfType(foundElement, DPLExpressionDefinition.class);
        if (expression != null) {
            return expression.getTextRange().getStartOffset();
        }
        if (TokenType.WHITE_SPACE.equals(foundElement.getNode().getElementType())) {
            return foundElement.getTextRange().getEndOffset();
        }
        return foundElement.getTextRange().getStartOffset();
    }

    private int getEndOffsetFromSelection(int selectionEnd, @NotNull PsiElement element) {
        PsiElement foundElement = element.getContainingFile().findElementAt(selectionEnd);
        if (foundElement == null) {
            return selectionEnd;
        }
        DPLExpressionDefinition expression = PsiTreeUtil.getParentOfType(foundElement, DPLExpressionDefinition.class);
        return Objects.requireNonNullElse(expression, foundElement).getTextRange().getEndOffset();
    }
}

package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractAddElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLAssignExpression;
import pl.thedeem.intellij.dql.psi.DQLExpression;

public class SetFieldNameQuickFix extends AbstractAddElementQuickFix {
    @Override
    protected int getCaretPosition(@NotNull PsiElement element) {
        return element.getTextRange().getStartOffset();
    }

    @Override
    protected @Nullable Template prepareTemplate(@NotNull PsiElement element, @NotNull TemplateManager templateManager, @NotNull Document document) {
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        if (element instanceof DQLAssignExpression assignExpression) {
            String initialValue = assignExpression.getFieldName();
            DQLExpression leftExpression = assignExpression.getLeftExpression();
            TextRange textRange = leftExpression != null ? leftExpression.getTextRange() : element.getTextRange();
            document.deleteString(textRange.getStartOffset(), textRange.getEndOffset());
            template.addVariable("fieldName", new TextExpression(initialValue), new EmptyNode(), true);
        } else {
            template.addVariable("fieldName", new EmptyNode(), new EmptyNode(), true);
            template.addTextSegment("=");
        }
        return template;
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

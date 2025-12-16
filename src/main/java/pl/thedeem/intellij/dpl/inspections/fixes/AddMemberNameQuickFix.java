package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractAddElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;

public class AddMemberNameQuickFix extends AbstractAddElementQuickFix {
    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.add.memberName");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }

    @Override
    protected int getCaretPosition(@NotNull PsiElement element) {
        return element.getTextRange().getEndOffset();
    }

    @Override
    protected Template prepareTemplate(@NotNull PsiElement element, @NotNull TemplateManager templateManager, @NotNull Document document) {
        Template template = templateManager.createTemplate("", "");
        template.addTextSegment(":");
        template.addVariable("memberName", new TextExpression(""), new EmptyNode(), true);
        return template;
    }
}

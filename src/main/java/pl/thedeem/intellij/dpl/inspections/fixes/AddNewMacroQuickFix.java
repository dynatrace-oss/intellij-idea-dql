package pl.thedeem.intellij.dpl.inspections.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractAddElementQuickFix;
import pl.thedeem.intellij.dpl.DPLBundle;

public class AddNewMacroQuickFix extends AbstractAddElementQuickFix {
    @NotNull
    private final String macroName;

    public AddNewMacroQuickFix(@NotNull String macroName) {
        this.macroName = macroName;
    }

    @Override
    public @IntentionName @NotNull String getName() {
        return DPLBundle.message("inspection.fix.add.macro");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return DPLBundle.message("inspection.fixesFamilyName");
    }

    @Override
    protected int getCaretPosition(@NotNull PsiElement element) {
        return 0;
    }

    @Override
    protected Template prepareTemplate(@NotNull PsiElement element, @NotNull TemplateManager templateManager) {
        Template template = templateManager.createTemplate("", "");
        template.addTextSegment(this.macroName);
        template.addTextSegment("=");
        template.addVariable("macroDefinition", new TextExpression(""), new EmptyNode(), true);
        template.addTextSegment(";");
        return template;
    }
}

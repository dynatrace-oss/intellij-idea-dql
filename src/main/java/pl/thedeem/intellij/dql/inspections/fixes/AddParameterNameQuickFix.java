package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractAddElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;

public class AddParameterNameQuickFix extends AbstractAddElementQuickFix {
    @SafeFieldForPreview
    private final MappedParameter parameter;

    public AddParameterNameQuickFix(@NotNull MappedParameter parameter) {
        this.parameter = parameter;
    }

    @Override
    protected int getCaretPosition(@NotNull PsiElement element) {
        return element.getTextRange().getStartOffset();
    }

    @Override
    protected Template prepareTemplate(@NotNull PsiElement element, @NotNull TemplateManager templateManager, @NotNull Document document) {
        Parameter definition = parameter.definition();
        if (definition == null) {
            return null;
        }
        Template template = templateManager.createTemplate("", "");
        template.addVariable("parameterName", new TextExpression(definition.name()), new EmptyNode(), true);
        template.addTextSegment(":");
        return template;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.fix.addParameterName");
    }
}

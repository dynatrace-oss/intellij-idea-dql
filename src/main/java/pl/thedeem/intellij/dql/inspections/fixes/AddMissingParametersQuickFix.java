package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.quickFixes.AbstractAddElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.completion.insertions.InsertionsUtils;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.util.Collection;

public class AddMissingParametersQuickFix extends AbstractAddElementQuickFix {
    @SafeFieldForPreview
    private final Collection<Parameter> missing;
    private final int offset;
    private final boolean addComa;

    public AddMissingParametersQuickFix(@NotNull Collection<Parameter> missingParameters, int offset, boolean addComa) {
        this.missing = missingParameters;
        this.offset = offset;
        this.addComa = addComa;
    }

    @Override
    protected int getCaretPosition(@NotNull PsiElement element) {
        return offset;
    }

    @Override
    protected Template prepareTemplate(@NotNull PsiElement element, @NotNull TemplateManager templateManager, @NotNull Document document) {
        Template template = templateManager.createTemplate("", "");
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
        return template;
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

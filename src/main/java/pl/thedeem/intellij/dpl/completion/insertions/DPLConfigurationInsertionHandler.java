package pl.thedeem.intellij.dpl.completion.insertions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.LangUtils;
import pl.thedeem.intellij.dpl.definition.model.Configuration;

import java.util.Objects;
import java.util.Set;

public class DPLConfigurationInsertionHandler implements InsertHandler<LookupElement> {
    private final static Set<Character> VALID_CHARACTERS_BEHIND = Set.of(',', '(');
    private final @NotNull Configuration definition;

    public DPLConfigurationInsertionHandler(@NotNull Configuration definition) {
        this.definition = definition;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        Editor editor = context.getEditor();
        int startOffset = context.getStartOffset();

        TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        if (LangUtils.shouldAddSeparatorBefore(context, VALID_CHARACTERS_BEHIND)) {
            template.addTextSegment(",");
        }

        template.addTextSegment(definition.name());
        template.addTextSegment("=");
        boolean wrapWithQuotes = definition.type() == null || "string".equals(definition.type());

        if (wrapWithQuotes) {
            template.addTextSegment("\"");
        }

        template.addVariable(
                definition.name(),
                new TextExpression(Objects.requireNonNullElse(definition.defaultValue(), "").toString()),
                new EmptyNode(),
                true
        );

        if (wrapWithQuotes) {
            template.addTextSegment("\"");
        }

        context.getDocument().deleteString(startOffset, context.getTailOffset());

        editor.getCaretModel().moveToOffset(startOffset);
        templateManager.startTemplate(editor, template);

        AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, context.getFile());
    }
}

package pl.thedeem.intellij.dql.completion.insertions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.LangUtils;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.util.Set;

public class DQLNamedParameterInsertionHandler implements InsertHandler<LookupElement> {
    private final static Set<Character> VALID_CHARACTERS_BEHIND = Set.of(',', '(', '{', '[');
    private final Parameter parameter;
    private final boolean addComma;

    public DQLNamedParameterInsertionHandler(Parameter parameter, boolean addComma) {
        this.parameter = parameter;
        this.addComma = addComma;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        Editor editor = context.getEditor();
        int startOffset = context.getStartOffset();

        TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        if (addComma) {
            Character lastNonEmptyCharacter = LangUtils.getPreviousNonEmptyCharacterFromDocument(context);
            if (lastNonEmptyCharacter != null && !VALID_CHARACTERS_BEHIND.contains(lastNonEmptyCharacter)) {
                template.addTextSegment(", ");
            }
        }

        if (parameter.requiresName()) {
            template.addTextSegment(parameter.name() + ": ");
        }

        InsertionsUtils.handleInsertionDefaultValue(parameter, template);

        context.getDocument().deleteString(startOffset, context.getTailOffset());

        editor.getCaretModel().moveToOffset(startOffset);
        templateManager.startTemplate(editor, template);

        AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, context.getFile());
    }
}

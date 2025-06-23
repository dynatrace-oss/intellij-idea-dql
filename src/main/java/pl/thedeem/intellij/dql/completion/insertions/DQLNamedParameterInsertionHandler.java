package pl.thedeem.intellij.dql.completion.insertions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.sdk.model.DQLDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DQLNamedParameterInsertionHandler implements InsertHandler<LookupElement> {
    private final static Set<Character> VALID_CHARACTERS_BEHIND = Set.of(',', '(', '{', '[');
    private final String paramName;
    private final Set<DQLDataType> allowedTypes;
    private final boolean addComma;
    private final String defaultValue;

    public DQLNamedParameterInsertionHandler(String paramName, Set<DQLDataType> allowedTypes, boolean addComma, String defaultValue) {
        this.paramName = paramName;
        this.allowedTypes = allowedTypes;
        this.addComma = addComma;
        this.defaultValue = defaultValue;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        Editor editor = context.getEditor();
        int startOffset = context.getStartOffset();

        TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        if (addComma) {
            Character lastNonEmptyCharacter = DQLUtil.getLastNonEmptyCharacterFromDocument(context);
            if (lastNonEmptyCharacter != null && !VALID_CHARACTERS_BEHIND.contains(lastNonEmptyCharacter)) {
                template.addTextSegment(", ");
            }
        }

        template.addTextSegment(paramName + ": ");

        InsertionsUtils.handleInsertionDefaultValue(allowedTypes, template, defaultValue, paramName);

        context.getDocument().deleteString(startOffset, context.getTailOffset());

        editor.getCaretModel().moveToOffset(startOffset);
        templateManager.startTemplate(editor, template);

        AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, context.getFile());
    }
}

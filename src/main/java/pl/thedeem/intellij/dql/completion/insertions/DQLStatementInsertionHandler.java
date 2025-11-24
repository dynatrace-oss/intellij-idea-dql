package pl.thedeem.intellij.dql.completion.insertions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import pl.thedeem.intellij.common.LangUtils;
import pl.thedeem.intellij.dql.definition.DQLCommandDefinition;
import pl.thedeem.intellij.dql.definition.DQLCommandGroup;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DQLStatementInsertionHandler implements InsertHandler<LookupElement> {
    private final DQLCommandDefinition definition;

    public DQLStatementInsertionHandler(@NotNull DQLCommandDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        Editor editor = context.getEditor();
        int startOffset = context.getStartOffset();

        TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        Character lastNonEmptyCharacter = LangUtils.getPreviousNonEmptyCharacterFromDocument(context);
        if ((lastNonEmptyCharacter == null || lastNonEmptyCharacter != '|') && !DQLCommandGroup.STARTING_COMMAND_TYPES.contains(definition.getCommandGroup())) {
            template.addTextSegment("| ");
        }

        template.addTextSegment(definition.name);
        template.addTextSegment(" ");

        handleRequiredParameters(template);

        context.getDocument().deleteString(startOffset, context.getTailOffset());

        editor.getCaretModel().moveToOffset(startOffset);
        templateManager.startTemplate(editor, template);

        AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, context.getFile());
    }

    private void handleRequiredParameters(Template template) {
        int i = 0;
        List<DQLParameterDefinition> requiredParameters = definition.getRequiredParameters();
        if (!requiredParameters.isEmpty()) {
            for (DQLParameterDefinition param : requiredParameters) {
                if (i > 0) {
                    template.addTextSegment(", ");
                }
                if (param.canBeNamed()) {
                    template.addTextSegment(param.name + ": ");
                }
                InsertionsUtils.handleInsertionDefaultValue(param.getDQLTypes(), template, param.defaultValue, param.name);
                i++;
            }
        }
    }
}

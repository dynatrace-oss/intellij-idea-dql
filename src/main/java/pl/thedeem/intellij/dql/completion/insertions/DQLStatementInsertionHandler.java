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
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.Parameter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DQLStatementInsertionHandler implements InsertHandler<LookupElement> {
    private final Command definition;

    public DQLStatementInsertionHandler(@NotNull Command definition) {
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
        if ((lastNonEmptyCharacter == null || lastNonEmptyCharacter != '|') && DQLDefinitionService.EXTENSION_COMMANDS.test(definition.category())) {
            template.addTextSegment("| ");
        }

        template.addTextSegment(definition.name());
        template.addTextSegment(" ");

        handleRequiredParameters(template);

        context.getDocument().deleteString(startOffset, context.getTailOffset());

        editor.getCaretModel().moveToOffset(startOffset);
        templateManager.startTemplate(editor, template);

        AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, context.getFile());
    }

    private void handleRequiredParameters(Template template) {
        int i = 0;
        Collection<Parameter> requiredParameters = definition.requiredParameters();
        Set<String> blockedNames = new HashSet<>();
        if (!requiredParameters.isEmpty()) {
            for (Parameter param : requiredParameters) {
                if (!blockedNames.contains(param.name())) {
                    if (i > 0) {
                        template.addTextSegment(", ");
                    }
                    if (param.requiresName()) {
                        template.addTextSegment(param.name() + ": ");
                    }
                    InsertionsUtils.handleInsertionDefaultValue(param, template);
                    i++;
                    blockedNames.add(param.name());
                    if (param.aliases() != null) {
                        blockedNames.addAll(param.aliases());
                    }
                    if (param.excludes() != null) {
                        blockedNames.addAll(param.excludes());
                    }
                }
            }
        }
    }
}

package pl.thedeem.intellij.dql.completion.insertions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.openapi.editor.Editor;
import pl.thedeem.intellij.dql.definition.DQLFunctionDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DQLFunctionInsertionHandler implements InsertHandler<LookupElement> {
    private final DQLFunctionDefinition definition;

    public DQLFunctionInsertionHandler(@NotNull DQLFunctionDefinition definition) {
        this.definition = definition;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        Editor editor = context.getEditor();
        int startOffset = context.getStartOffset();

        TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        template.addTextSegment(definition.name);
        template.addTextSegment("(");

        List<DQLParameterDefinition> parameters = definition.getRequiredParameters();
        for (int i = 0; i < parameters.size(); i++) {
            DQLParameterDefinition param = parameters.get(i);
            template.addVariable(param.name, new TextExpression(param.name), new EmptyNode(), true);
            if (i < parameters.size() - 1) {
                template.addTextSegment(", ");
            }
        }

        template.addTextSegment(")");

        context.getDocument().deleteString(startOffset, context.getTailOffset());

        editor.getCaretModel().moveToOffset(startOffset);
        templateManager.startTemplate(editor, template);

        AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, context.getFile());
    }
}

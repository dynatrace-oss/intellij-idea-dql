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
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.definition.model.Signature;

import java.util.List;

public class DQLFunctionInsertionHandler implements InsertHandler<LookupElement> {
    private final Function definition;
    private final Signature signature;

    public DQLFunctionInsertionHandler(@NotNull Function definition, @NotNull Signature signature) {
        this.definition = definition;
        this.signature = signature;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        Editor editor = context.getEditor();
        int startOffset = context.getStartOffset();

        TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        template.addTextSegment(definition.name());
        template.addTextSegment("(");

        List<Parameter> parameters = signature.requiredParameters();
        for (int i = 0; i < parameters.size(); i++) {
            Parameter param = parameters.get(i);
            if (param.requiresName()) {
                template.addTextSegment(param.name() + ":");
            }
            template.addVariable(param.name(), new TextExpression(param.name()), new EmptyNode(), true);
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

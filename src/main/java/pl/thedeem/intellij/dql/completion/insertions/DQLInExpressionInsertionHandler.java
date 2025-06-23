package pl.thedeem.intellij.dql.completion.insertions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.EmptyNode;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public class DQLInExpressionInsertionHandler implements InsertHandler<LookupElement> {

  @Override
  public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
    Editor editor = context.getEditor();
    int startOffset = context.getStartOffset();

    TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
    Template template = templateManager.createTemplate("", "");
    template.setToReformat(true);

    template.addTextSegment("in [");
    template.addVariable("subquery", new EmptyNode(), new EmptyNode(), true);
    template.addTextSegment("]");

    context.getDocument().deleteString(startOffset, context.getTailOffset());

    editor.getCaretModel().moveToOffset(startOffset);
    templateManager.startTemplate(editor, template);

    AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, context.getFile());
  }
}

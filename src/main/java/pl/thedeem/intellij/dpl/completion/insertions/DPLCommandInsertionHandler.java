package pl.thedeem.intellij.dpl.completion.insertions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.LangUtils;
import pl.thedeem.intellij.dpl.psi.DPLAlternativesExpression;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;

import java.util.Set;

public class DPLCommandInsertionHandler implements InsertHandler<LookupElement> {
    private final static Set<Character> VALID_ALT_GROUP_CHARACTERS_BEHIND = Set.of('(', '|');

    private final @NotNull String commandName;

    public DPLCommandInsertionHandler(@NotNull String commandName) {
        this.commandName = commandName;
    }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement lookupElement) {
        Editor editor = context.getEditor();
        int startOffset = context.getStartOffset();
        PsiElement psiElement = context.getFile().findElementAt(Math.max(0, context.getTailOffset() - 1));
        DPLAlternativesExpression group = PsiTreeUtil.getParentOfType(psiElement, DPLAlternativesExpression.class);

        TemplateManager templateManager = TemplateManager.getInstance(context.getProject());
        Template template = templateManager.createTemplate("", "");
        template.setToReformat(true);

        DPLExpressionDefinition firstElement = group != null ? group.getExpressionDefinitionList().getFirst() : null;

        if (firstElement != null && firstElement.getTextOffset() < startOffset && LangUtils.shouldAddSeparatorBefore(context, VALID_ALT_GROUP_CHARACTERS_BEHIND)) {
            template.addTextSegment("|");
        }
        template.addTextSegment(commandName.toUpperCase());

        context.getDocument().deleteString(startOffset, context.getTailOffset());

        editor.getCaretModel().moveToOffset(startOffset);
        templateManager.startTemplate(editor, template);

        AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, context.getFile());
    }
}

package pl.thedeem.intellij.dql.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.psi.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DQLSplitFilterCommand extends PsiElementBaseIntentionAction implements Iconable {
    private final static Set<String> FILTERABLE_COMMANDS = Set.of("filter", "filterOut");

    public DQLSplitFilterCommand() {
        setText(DQLBundle.message("intention.splitFilterCommands"));
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        DQLCommand command = PsiTreeUtil.getParentOfType(psiElement, DQLCommand.class);
        if (command == null || command.getDefinition() == null || !FILTERABLE_COMMANDS.contains(command.getDefinition().name())) {
            return false;
        }
        MappedParameter condition = command.findParameter("condition");
        if (condition == null) {
            return false;
        }
        return condition.getExpressions().stream().anyMatch(e -> {
            PsiElement toCheck = e;
            if (toCheck instanceof DQLParenthesisedExpression parents) {
                toCheck = parents.getExpression();
            }
            return toCheck instanceof DQLConditionExpression cond && isJoiningCondition(cond);
        });
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        Document document = editor.getDocument();
        DQLCommand command = PsiTreeUtil.getParentOfType(psiElement, DQLCommand.class);
        if (command == null || command.getDefinition() == null) {
            return;
        }
        MappedParameter condition = command.findParameter("condition");
        if (condition == null || condition.definition() == null) {
            return;
        }
        String baseCommand = command.getDefinition().name();
        List<DQLExpression> expressions = getExpressionsToSeparate(condition);
        String value = String.join("\n", expressions.stream().map(e -> "| " + baseCommand + " " + e.getText()).toList());
        document.replaceString(
                command.getTextRange().getStartOffset(),
                command.getTextRange().getEndOffset(),
                value
        );
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DynatraceQueryLanguage.DQL_DISPLAY_NAME;
    }

    @Override
    public Icon getIcon(int i) {
        return DQLIcon.INTENTION;
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    private @NotNull List<DQLExpression> getExpressionsToSeparate(@NotNull MappedParameter condition) {
        List<DQLExpression> result = new ArrayList<>();
        List<PsiElement> toProcess = new ArrayList<>(condition.getExpressions());

        while (!toProcess.isEmpty()) {
            PsiElement toCheck = DQLUtil.unpackParenthesis(toProcess.removeFirst());
            if (toCheck instanceof DQLParenthesisedExpression parents) {
                toCheck = parents.getExpression();
            }
            if (toCheck instanceof DQLConditionExpression cond && isJoiningCondition(cond)) {
                toProcess.addAll(cond.getExpressionList());
            } else if (toCheck instanceof DQLExpression expression) {
                result.add(expression);
            }
        }

        return result;
    }

    private boolean isJoiningCondition(@NotNull DQLConditionExpression condition) {
        return DQLTypes.AND == condition.getConditionOperator().getNode().getFirstChildNode().getElementType();
    }
}

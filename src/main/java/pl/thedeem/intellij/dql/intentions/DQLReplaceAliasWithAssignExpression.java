package pl.thedeem.intellij.dql.intentions;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.psi.*;
import pl.thedeem.intellij.dql.psi.elements.DQLParametersOwner;

import javax.swing.*;
import java.util.List;
import java.util.Set;

public class DQLReplaceAliasWithAssignExpression extends PsiElementBaseIntentionAction implements Iconable {
    public DQLReplaceAliasWithAssignExpression() {
        setText(DQLBundle.message("intention.replaceAliasWithAssignExpression"));
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

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) {
        DQLParameterExpression argument = findParameter(psiElement);
        MappedParameter parameter = findParameter(argument);
        PsiElement aliasedExpression = parameter != null ? findAliasedExpression(argument, parameter) : null;
        return aliasedExpression != null;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement psiElement) throws IncorrectOperationException {
        Document document = editor.getDocument();
        DQLParameterExpression argument = findParameter(psiElement);
        MappedParameter parameter = findParameter(argument);
        if (parameter == null) {
            return;
        }
        PsiElement aliasedExpression = findAliasedExpression(argument, parameter);
        if (aliasedExpression == null) {
            return;
        }
        DQLExpression value = argument.getExpression();
        if (value == null) {
            return;
        }
        document.deleteString(aliasedExpression.getTextRange().getEndOffset(), argument.getTextRange().getEndOffset());
        document.insertString(aliasedExpression.getTextRange().getStartOffset(), value.getText() + " = ");
    }

    private @Nullable PsiElement findAliasedExpression(@NotNull DQLParameterExpression argument, @NotNull MappedParameter parameter) {
        List<PsiElement> expressions = parameter.unpackExpressions();
        PsiElement aliasedValue = null;
        for (int i = 0; i < expressions.size(); i++) {
            if (expressions.get(i).getTextRange().intersects(argument.getTextRange())) {
                if (i == 0) {
                    return null;
                }
                aliasedValue = expressions.get(i - 1);
                break;
            }
        }

        if (aliasedValue == null || aliasedValue instanceof DQLAssignExpression) {
            return null;
        }
        return aliasedValue;
    }

    private @Nullable DQLParameterExpression findParameter(@NotNull PsiElement element) {
        if (!(element.getParent() instanceof DQLParameterName paramName)
                || !(paramName.getParent() instanceof DQLParameterExpression param)
                || !"alias".equalsIgnoreCase(param.getName())) {
            return null;
        }
        return param;
    }

    private @Nullable MappedParameter findParameter(@Nullable DQLParameterExpression expression) {
        if (expression == null) {
            return null;
        }
        List<PsiElement> parents = PsiUtils.getElementsUntilParent(
                expression,
                s -> Set.of(DQLBracketExpression.class).stream().anyMatch(c -> c.isInstance(s)),
                DQLParametersOwner.class);

        PsiElement paramExpression = parents.size() > 1 ? parents.get(1) : expression;
        if (!((parents.getFirst()) instanceof DQLParametersOwner owner)) {
            return null;
        }
        MappedParameter parameter = owner.getParameter(paramExpression);
        if (parameter == null || parameter.definition() == null || !parameter.definition().allowsFieldName()) {
            return null;
        }
        return parameter;
    }
}

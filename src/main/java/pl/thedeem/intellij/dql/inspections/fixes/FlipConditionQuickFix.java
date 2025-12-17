package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.quickFixes.AbstractReplaceElementQuickFix;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLUnaryExpression;

public class FlipConditionQuickFix extends AbstractReplaceElementQuickFix<DQLQueryStatement> {
    @Override
    protected @Nullable DQLQueryStatement getElementToReplace(@NotNull PsiElement element) {
        return element instanceof DQLQueryStatement command ? command : PsiTreeUtil.getParentOfType(element, DQLQueryStatement.class);
    }

    @Override
    protected @Nullable String getDefaultReplacement(@NotNull DQLQueryStatement command) {
        Command definition = command.getDefinition();
        if (definition == null) {
            return null;
        }
        MappedParameter condition = command.findParameter("condition");
        if (condition == null) {
            return null;
        }
        return "|" + DQLDefinitionService.FILTER_COMMAND_NEGATIONS.get(definition.name()) + " " +
                String.join(",", condition.getExpressions().stream().map(expression -> {
                    if (DQLUtil.unpackParenthesis(expression) instanceof DQLUnaryExpression unary) {
                        return unary.getExpression() != null ? unary.getExpression().getText() : "";
                    } else {
                        return "not " + expression.getText();
                    }
                }).toList());
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return DQLBundle.message("inspection.fix.genericFamilyName");
    }

    @NotNull
    @Override
    public String getName() {
        return DQLBundle.message("inspection.negatedFilteringCondition.fix.flipFilterCommand");
    }

    @Override
    protected boolean useTemplateVariable() {
        return false;
    }

    @Override
    protected boolean reformatTemplate() {
        return true;
    }
}

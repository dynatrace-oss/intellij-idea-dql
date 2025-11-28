package pl.thedeem.intellij.dpl.completion.providers;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.completion.DPLCompletions;
import pl.thedeem.intellij.dpl.completion.DPLPsiPatterns;
import pl.thedeem.intellij.dpl.definition.DPLDefinitionService;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.psi.DPLCommandExpression;
import pl.thedeem.intellij.dpl.psi.DPLCommandMatchers;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;

import java.util.Set;

public class DPLCommandCompletions {
    private static final Set<String> ALLOWED_MATCHERS = Set.of("members_list", "simple_expression", "matcher_expr");

    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();

        if (!DPLPsiPatterns.COMMAND.accepts(position)) {
            return;
        }
        if (DPLPsiPatterns.COMMAND_MATCHERS.accepts(position) && !canAutocompleteMatchers(position)) {
            return;
        }

        DPLDefinitionService service = DPLDefinitionService.getInstance(position.getProject());

        for (Command value : service.commands().values()) {
            result.addElement(DPLCompletions.createConfigurationParameterLookup(value));
        }
    }

    private boolean canAutocompleteMatchers(@NotNull PsiElement position) {
        DPLCommandMatchers matchers = PsiTreeUtil.getParentOfType(position, DPLCommandMatchers.class);
        if (matchers != null
                && matchers.getParent().getParent() instanceof DPLExpressionDefinition exprDef
                && exprDef.getExpression() instanceof DPLCommandExpression command) {
            Command definition = command.getDefinition();
            if (definition != null) {
                return definition.matchers() != null && ALLOWED_MATCHERS.contains(definition.matchers().type());
            }
        }
        return false;
    }
}

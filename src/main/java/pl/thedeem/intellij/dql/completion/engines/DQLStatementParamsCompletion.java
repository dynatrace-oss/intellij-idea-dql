package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.completion.DQLPsiPatterns;
import pl.thedeem.intellij.dql.definition.DQLCommandDefinition;
import pl.thedeem.intellij.dql.definition.DQLCommandsLoader;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DQLStatementParamsCompletion implements DQLCompletionEngine {
    @Override
    public CompletionResult autocomplete(@NotNull PsiElement position, @NotNull CompletionResultSet result) {
        if (DQLPsiPatterns.INSIDE_STATEMENT_PARAMETERS_LIST.accepts(position)) {
            DQLQueryStatement statement = PsiTreeUtil.getParentOfType(position, DQLQueryStatement.class);
            DQLCommandDefinition definition = statement != null ? DQLCommandsLoader.getCommand(statement) : null;
            if (statement != null && definition != null) {
                List<DQLParameterObject> paramsList = statement.getParameters();
                Set<String> definedParameters = getExcludedParameters(paramsList);
                for (DQLParameterDefinition parameter : definition.parameters) {
                    if (parameter.canBeNamed() && !definedParameters.contains(parameter.name)) {
                        boolean addComma = !paramsList.isEmpty() && !CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED.equals(paramsList.getFirst().getExpression().getText());
                        AutocompleteUtils.autocompleteParameter(parameter, result, addComma);
                    }
                }
            }
        }
        return CompletionResult.PASS;
    }

    private Set<String> getExcludedParameters(List<DQLParameterObject> paramsList) {
        Set<String> excluded = new HashSet<>();
        if (paramsList != null) {
            for (DQLParameterObject parameter : paramsList) {
                DQLParameterDefinition definition = parameter.getDefinition();
                if (parameter.getValues().size() == 1 && CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED.equals(parameter.getValues().getFirst().getText())) {
                    continue;
                }
                if (definition != null && (parameter.isNamed() || !definition.canBeNamed())) {
                    excluded.add(definition.name);
                    if (definition.disallows != null && !definition.disallows.isEmpty()) {
                        excluded.addAll(definition.disallows);
                    }
                }
            }
        }
        return excluded;
    }
}

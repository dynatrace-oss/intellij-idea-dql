package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.psi.PsiUtils;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Function;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLExpression;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DQLFieldValuesCompletions {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        List<PsiElement> parent = PsiUtils.getElementsUntilParent(position, DQLFunctionCallExpression.class, DQLQueryStatement.class);
        if (parent.getFirst() instanceof DQLFunctionCallExpression function && parent.get(1) instanceof DQLExpression expression) {
            autocompleteParameter(function.getParameter(expression), result);
        } else if (parent.getFirst() instanceof DQLQueryStatement list && parent.get(1) instanceof DQLExpression expression) {
            autocompleteParameter(list.getParameter(expression), result);
        }
    }

    private void autocompleteParameter(@Nullable MappedParameter parameter, @NotNull CompletionResultSet result) {
        Parameter definition = parameter != null ? parameter.definition() : null;
        if (definition == null) {
            return;
        }
        if (definition.requiresName() && !parameter.isExplicitlyNamed()) {
            return;
        }
        if (definition.allowedEnumValues() != null) {
            for (String enumValue : definition.allowedEnumValues()) {
                AutocompleteUtils.autocompleteStaticValue(enumValue, result);
            }
        } else {
            DQLDefinitionService service = DQLDefinitionService.getInstance(parameter.holder().getProject());
            List<String> parameterValueTypes = Objects.requireNonNullElse(definition.parameterValueTypes(), List.of());
            List<String> valueTypes = Objects.requireNonNullElse(definition.valueTypes(), List.of());

            if (definition.defaultValue() != null) {
                AutocompleteUtils.autocompleteStaticValue(definition.defaultValue(), result);
            }
            Collection<String> categories = service.getFunctionCategoriesForParameterTypes(parameterValueTypes);
            if (categories == null) {
                return;
            }
            Collection<Function> matchingFunctions = service.getFunctionsByCategoryAndReturnType(
                    s -> categories.isEmpty() || categories.contains(s),
                    s -> valueTypes.isEmpty() || valueTypes.contains(s)
            );
            for (Function function : matchingFunctions) {
                AutocompleteUtils.autocomplete(function, result);
            }
            if (valueTypes.contains("dql.dataType.boolean")) {
                AutocompleteUtils.autocompleteBooleans(result);
            }
            if (valueTypes.contains("dql.dataType.timestamp")) {
                AutocompleteUtils.autocompleteCurrentTimestamp(result);
            }
        }
    }
}

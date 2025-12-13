package pl.thedeem.intellij.dql.completion.engines;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.completion.AutocompleteUtils;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;

import java.util.List;
import java.util.Objects;

public class DQLStatementParamsCompletion {
    public void autocomplete(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        PsiElement position = parameters.getPosition();
        DQLQueryStatement statement = PsiTreeUtil.getParentOfType(position, DQLQueryStatement.class);
        DQLDefinitionService service = DQLDefinitionService.getInstance(position.getProject());
        Command definition = statement != null ? service.getCommandByName(Objects.requireNonNull(statement.getName())) : null;
        if (statement != null && definition != null) {
            List<MappedParameter> definedParams = statement.getParameters();
            boolean addComma = !definedParams.isEmpty() && !CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED.equals(definedParams.getFirst().holder().getText());
            for (Parameter parameter : statement.getMissingParameters()) {
                AutocompleteUtils.autocomplete(parameter, result, addComma);
            }
        }
    }
}

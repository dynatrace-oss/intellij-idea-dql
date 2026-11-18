package pl.thedeem.intellij.dql.services.query;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;

import java.util.*;

public class DQLQueryParserServiceImpl implements DQLQueryParserService {
    @NotNull
    public ParseResult getSubstitutedQuery(@NotNull PsiFile psiFile, @Nullable List<DQLVariablesService.VariableDefinition> variableDefinitions) {
        Map<String, String> definitions = getFoundVariables(psiFile, variableDefinitions);

        return ReadAction.compute(() -> {
            String originalText = psiFile.getText();
            List<DQLVariableExpression> vars = new ArrayList<>(PsiTreeUtil.findChildrenOfType(psiFile, DQLVariableExpression.class));
            vars.sort(Comparator.comparingInt(v -> v.getTextRange().getStartOffset()));

            List<OffsetMapping> mappings = new ArrayList<>();
            StringBuilder substituted = new StringBuilder(originalText.length());
            int lastOriginalOffset = 0;
            for (DQLVariableExpression variable : vars) {
                TextRange range = variable.getTextRange();
                if (range.getStartOffset() < lastOriginalOffset) {
                    continue;
                }
                String value = Objects.requireNonNullElse(definitions.get(variable.getName()), "null");
                substituted.append(originalText, lastOriginalOffset, range.getStartOffset());
                mappings.add(new OffsetMapping(range.getStartOffset(), substituted.length(), range.getLength(), value.length()));
                substituted.append(value);
                lastOriginalOffset = range.getEndOffset();
            }
            substituted.append(originalText, lastOriginalOffset, originalText.length());
            return new ParseResult(mappings, substituted.toString());
        });
    }

    @Override
    public @NotNull ParseResult getSubstitutedQuery(@NotNull String query, @NotNull Project project, @Nullable List<DQLVariablesService.VariableDefinition> variableDefinitions) {
        PsiFile file = PsiFileFactory.getInstance(project)
                .createFileFromText("temporary" + DQLFileType.INSTANCE.getDefaultExtension(), DQLFileType.INSTANCE, query);
        return getSubstitutedQuery(file, variableDefinitions);
    }

    private Map<String, String> getFoundVariables(@NotNull PsiFile psiFile, @Nullable List<DQLVariablesService.VariableDefinition> variableDefinitions) {
        return ReadAction.nonBlocking(() -> {
            Collection<DQLVariableExpression> variables = PsiTreeUtil.findChildrenOfType(psiFile, DQLVariableExpression.class);
            Map<String, String> defs = new HashMap<>();
            for (DQLVariableExpression variable : variables) {
                String value = variable.getValue();
                if (value != null) {
                    defs.put(variable.getName(), value);
                }
            }
            if (variableDefinitions != null) {
                for (DQLVariablesService.VariableDefinition defined : variableDefinitions) {
                    defs.put(defined.name(), defined.value());
                }
            }
            return defs;
        }).executeSynchronously();
    }
}

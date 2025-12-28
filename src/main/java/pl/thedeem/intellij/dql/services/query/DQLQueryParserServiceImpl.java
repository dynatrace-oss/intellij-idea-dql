package pl.thedeem.intellij.dql.services.query;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.psi.DQLElementFactory;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;

import java.util.*;

public class DQLQueryParserServiceImpl implements DQLQueryParserService {
    @NotNull
    public ParseResult getSubstitutedQuery(@NotNull PsiFile psiFile, @Nullable List<DQLVariablesService.VariableDefinition> variableDefinitions) {
        Map<String, String> definitions = getFoundVariables(psiFile, variableDefinitions);

        PsiElement copied = psiFile.copy();
        List<DQLVariableExpression> vars = new ArrayList<>(PsiTreeUtil.findChildrenOfType(copied, DQLVariableExpression.class));
        vars.sort(Comparator.comparingInt(PsiElement::getTextOffset));

        List<OffsetMapping> mappings = new ArrayList<>();
        List<Replacement> replacements = new ArrayList<>();

        // collect the information about variable positions
        for (DQLVariableExpression variable : vars) {
            String name = variable.getName();
            String value = definitions.get(name);
            if (value == null) {
                value = "null";
            }

            int originalStartOffset = variable.getTextOffset();
            String originalText = variable.getText();
            int originalLength = originalText.length();
            int substitutedLength = value.length();

            mappings.add(new OffsetMapping(originalStartOffset, -1, originalLength, substitutedLength));
            replacements.add(new Replacement(variable, value));
        }

        // update variables and substituted offsets
        for (int i = 0; i < replacements.size(); i++) {
            Replacement r = replacements.get(i);
            int substitutedStartOffset = r.variable().getTextOffset();

            mappings.set(i, new OffsetMapping(
                    mappings.get(i).originalStartOffset(),
                    substitutedStartOffset,
                    mappings.get(i).originalLength(),
                    mappings.get(i).substitutedLength()
            ));

            PsiElement replacementElement = DQLElementFactory.createUnknownElement(psiFile.getProject(), r.value());
            r.variable().replace(replacementElement);
        }

        return new ParseResult(mappings, copied.getText());
    }

    @Override
    public @NotNull ParseResult getSubstitutedQuery(@NotNull String query, @NotNull Project project, @Nullable List<DQLVariablesService.VariableDefinition> variableDefinitions) {
        PsiFile file = PsiFileFactory.getInstance(project)
                .createFileFromText("temporary" + DQLFileType.INSTANCE.getDefaultExtension(), DQLFileType.INSTANCE, query);
        return getSubstitutedQuery(file, variableDefinitions);
    }

    private Map<String, String> getFoundVariables(@NotNull PsiFile psiFile, @Nullable List<DQLVariablesService.VariableDefinition> variableDefinitions) {
        return ReadAction.compute(() -> {
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
        });
    }
}

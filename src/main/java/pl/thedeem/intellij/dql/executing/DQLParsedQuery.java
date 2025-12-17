package pl.thedeem.intellij.dql.executing;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.psi.DQLElementFactory;
import pl.thedeem.intellij.dql.psi.DQLVariableExpression;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class DQLParsedQuery {
    private final String parsedQuery;
    private final List<OffsetMapping> offsetMappings;

    public DQLParsedQuery(@NotNull PsiFile psiFile) {
        this.offsetMappings = new ArrayList<>();
        this.parsedQuery = getSubstitutedQuery(psiFile);
    }

    public String getSubstitutedQuery(PsiFile psiFile) {
        Map<String, String> definitions = getFoundVariables(psiFile);
        AtomicReference<String> result = new AtomicReference<>("");

        WriteCommandAction.runWriteCommandAction(psiFile.getProject(), () -> {
            PsiElement copied = psiFile.copy();
            List<DQLVariableExpression> vars = new ArrayList<>(PsiTreeUtil.findChildrenOfType(copied, DQLVariableExpression.class));
            vars.sort(Comparator.comparingInt(PsiElement::getTextOffset));

            List<OffsetMapping> mappings = new ArrayList<>();
            List<Replacement> replacements = new ArrayList<>();

            // collect the information about variable positions
            for (DQLVariableExpression variable : vars) {
                String name = variable.getName();
                String value = definitions.get(name);
                if (value != null) {
                    int originalStartOffset = variable.getTextOffset();
                    String originalText = variable.getText();
                    int originalLength = originalText.length();
                    int substitutedLength = value.length();

                    mappings.add(new OffsetMapping(originalStartOffset, -1, originalLength, substitutedLength));
                    replacements.add(new Replacement(variable, value));
                }
            }

            // update variables and substituted offsets
            for (int i = 0; i < replacements.size(); i++) {
                Replacement r = replacements.get(i);
                int substitutedStartOffset = r.variable.getTextOffset();

                mappings.set(i, new OffsetMapping(
                        mappings.get(i).originalStartOffset,
                        substitutedStartOffset,
                        mappings.get(i).originalLength,
                        mappings.get(i).substitutedLength
                ));

                PsiElement replacementElement = DQLElementFactory.createUnknownElement(psiFile.getProject(), r.value);
                r.variable.replace(replacementElement);
            }

            offsetMappings.addAll(mappings);
            result.set(copied.getText());
        });

        return result.get();
    }

    public String getParsedQuery() {
        return parsedQuery;
    }

    public int getOriginalOffset(int substitutedOffset) {
        if (substitutedOffset < 0 || substitutedOffset > parsedQuery.length()) {
            return -1;
        }

        int originalOffset = substitutedOffset;
        for (OffsetMapping mapping : offsetMappings) {
            // if it's inside the variable, let's just return the original "$" position
            if (substitutedOffset >= mapping.substitutedStartOffset
                    && substitutedOffset < (mapping.substitutedStartOffset + mapping.substitutedLength)) {
                return mapping.originalStartOffset;
            }

            if (substitutedOffset >= mapping.substitutedStartOffset) {
                originalOffset -= (mapping.substitutedLength - mapping.originalLength);
            } else {
                break;
            }
        }
        return Math.max(originalOffset, 0);
    }

    private Map<String, String> getFoundVariables(PsiFile psiFile) {
        return ReadAction.compute(() -> {
            Collection<DQLVariableExpression> variables = PsiTreeUtil.findChildrenOfType(psiFile, DQLVariableExpression.class);
            Map<String, String> defs = new HashMap<>();
            for (DQLVariableExpression variable : variables) {
                String value = variable.getValue();
                if (value != null) {
                    defs.put(variable.getName(), value);
                }
            }
            return defs;
        });
    }

    private record OffsetMapping(int originalStartOffset, int substitutedStartOffset, int originalLength,
                                 int substitutedLength) {
    }

    private record Replacement(DQLVariableExpression variable, String value) {
    }

}

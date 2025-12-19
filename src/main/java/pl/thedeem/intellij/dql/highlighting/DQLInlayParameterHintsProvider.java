package pl.thedeem.intellij.dql.highlighting;

import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.definition.model.Parameter;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;
import pl.thedeem.intellij.dql.psi.DQLParameterExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DQLInlayParameterHintsProvider implements InlayParameterHintsProvider {
    @Override
    public @NotNull List<InlayInfo> getParameterHints(@NotNull PsiElement element) {
        if (element instanceof DQLFunctionExpression functionCall) {
            return getParameterHints(functionCall.getParameters(), true);
        } else if (element instanceof DQLCommand statement) {
            return getParameterHints(statement.getParameters(), false);
        }
        return List.of();
    }

    private List<InlayInfo> getParameterHints(List<MappedParameter> parameters, boolean inlayFirstParameter) {
        List<InlayInfo> result = new ArrayList<>();
        if (parameters.size() > 1) {
            for (MappedParameter parameter : parameters) {
                Parameter definition = parameter.definition();
                if (definition == null) {
                    continue;
                }
                List<List<PsiElement>> groups = parameter.getParameterGroups();
                for (List<PsiElement> group : groups) {
                    if (group == groups.getFirst() && (parameter == parameters.getFirst() && !inlayFirstParameter)) {
                        continue;
                    }
                    int textOffset = group.getFirst().getTextOffset();
                    if (!(group.getFirst() instanceof DQLParameterExpression)) {
                        if (definition.variadic()) {
                            result.add(new InlayInfo((!parameter.included().isEmpty() ? "…" : "") + definition.name(), textOffset));
                        } else if (group.size() > 1) {
                            result.add(new InlayInfo(definition.name(), textOffset));
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public @NotNull Set<String> getDefaultBlackList() {
        return Set.of();
    }
}

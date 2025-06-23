package pl.thedeem.intellij.dql.highlighting;

import com.intellij.codeInsight.hints.InlayInfo;
import com.intellij.codeInsight.hints.InlayParameterHintsProvider;
import com.intellij.psi.PsiElement;
import pl.thedeem.intellij.dql.definition.DQLParameterDefinition;
import pl.thedeem.intellij.dql.definition.DQLParameterObject;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("UnstableApiUsage")
public class DQLInlayParameterHintsProvider implements InlayParameterHintsProvider {

    @Override
    public @NotNull List<InlayInfo> getParameterHints(@NotNull PsiElement element) {
        if (element instanceof DQLFunctionCallExpression functionCall) {
            return getParameterHints(functionCall.getParameters(), true);
        } else if (element instanceof DQLQueryStatement statement) {
            return getParameterHints(statement.getParameters(), false);
        }
        return List.of();
    }

    private List<InlayInfo> getParameterHints(List<DQLParameterObject> parameters, boolean inlayFirstParameter) {
        List<InlayInfo> result = new ArrayList<>();
        if (parameters.size() > 1) {
            for (DQLParameterObject parameter : parameters) {
                DQLParameterDefinition definition = parameter.getDefinition();
                if (parameter == parameters.getFirst() && !inlayFirstParameter) {
                    continue;
                }
                int textOffset = parameter.getExpression().getTextOffset();
                if (definition != null && !parameter.isNamed()) {
                    if (definition.repetitive) {
                        if (parameter.getExpression() == parameter.getValues().getFirst()) {
                            result.add(new InlayInfo((definition.repetitive && parameter.getValues().size() > 1 ? "…" : "") + definition.name, textOffset));
                        }
                    } else if (parameters.size() > 1) {
                        result.add(new InlayInfo(definition.name, textOffset));
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

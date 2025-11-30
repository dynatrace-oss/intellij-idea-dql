package pl.thedeem.intellij.dpl.inspections.matchers;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.CommandMatcher;
import pl.thedeem.intellij.dpl.inspections.fixes.DropKvpMatcherEntryQuickFix;
import pl.thedeem.intellij.dpl.psi.*;

public class InvalidKvpMatcherEntryValueInspection extends AbstractInvalidMatcherInspection {
    protected void validateMatcher(@NotNull DPLCommandMatchersContent definedMatchers, @NotNull DPLExpressionDefinition expression, @NotNull CommandMatcher matchersDefinition, @NotNull ProblemsHolder holder) {
        if (!"kvp".equals(matchersDefinition.type())) {
            return;
        }
        if (definedMatchers instanceof DPLParametersMatchersList params) {
            for (DPLMatcher matcher : params.getMatcherList()) {
                DPLSimpleExpression simpleExpression = matcher.getSimpleExpression();
                if (simpleExpression != null) {
                    String definedType = getExpressionType(simpleExpression);
                    if (!definedType.equals(matchersDefinition.value())) {
                        holder.registerProblem(
                                matcher,
                                DPLBundle.message("inspection.command.invalidKvpMatcherValueType", matchersDefinition.value(), definedType),
                                new DropKvpMatcherEntryQuickFix()
                        );
                    }
                }
            }
        }
    }

    private String getExpressionType(@NotNull DPLSimpleExpression expression) {
        return switch (expression) {
            case DPLBoolean ignored -> "boolean";
            case DPLNumber ignored -> "integer";
            case DPLString ignored -> "string";
            default -> "unknown";
        };
    }
}

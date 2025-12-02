package pl.thedeem.intellij.dpl.inspections.matchers;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.CommandMatcher;
import pl.thedeem.intellij.dpl.inspections.fixes.DropMatchersQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLCommandMatchersContent;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLParametersMatchersList;

public class InvalidKvpMatcherInspection extends AbstractInvalidMatcherInspection {
    protected void validateMatcher(@NotNull DPLCommandMatchersContent definedMatchers, @NotNull DPLExpressionDefinition expression, @NotNull CommandMatcher matchersDefinition, @NotNull ProblemsHolder holder) {
        if (!"kvp".equals(matchersDefinition.type())) {
            return;
        }
        if (!(definedMatchers instanceof DPLParametersMatchersList)) {
            holder.registerProblem(
                    definedMatchers,
                    DPLBundle.message("inspection.command.expectedKvpMatchers", matchersDefinition.key(), matchersDefinition.value()),
                    new DropMatchersQuickFix()
            );
        }
    }
}

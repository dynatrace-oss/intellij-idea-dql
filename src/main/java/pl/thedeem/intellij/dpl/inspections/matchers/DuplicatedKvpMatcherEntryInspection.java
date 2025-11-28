package pl.thedeem.intellij.dpl.inspections.matchers;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.CommandMatcher;
import pl.thedeem.intellij.dpl.inspections.fixes.DropKvpMatcherEntryQuickFix;
import pl.thedeem.intellij.dpl.inspections.fixes.ReplaceMatcherNameQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLCommandExpression;
import pl.thedeem.intellij.dpl.psi.DPLCommandMatchersContent;
import pl.thedeem.intellij.dpl.psi.DPLMatcher;
import pl.thedeem.intellij.dpl.psi.DPLParametersMatchersList;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DuplicatedKvpMatcherEntryInspection extends AbstractInvalidMatcherInspection {
    protected void validateMatcher(@NotNull DPLCommandMatchersContent definedMatchers, @NotNull DPLCommandExpression command, @NotNull CommandMatcher matchersDefinition, @NotNull ProblemsHolder holder) {
        if (!"kvp".equals(matchersDefinition.type())) {
            return;
        }
        if (definedMatchers instanceof DPLParametersMatchersList params) {
            Set<String> seen = new HashSet<>();
            for (DPLMatcher matcher : params.getMatcherList()) {
                String name = Objects.requireNonNullElse(matcher.getMatcherName().getName(), "").toLowerCase();
                if (!seen.add(name)) {
                    holder.registerProblem(
                            matcher,
                            DPLBundle.message("inspection.command.duplicatedKvpMatcherEntry", name),
                            new DropKvpMatcherEntryQuickFix(),
                            new ReplaceMatcherNameQuickFix()
                    );
                }
            }
        }
    }
}

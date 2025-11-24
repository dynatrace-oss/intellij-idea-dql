package pl.thedeem.intellij.dpl.inspections.matchers;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.CommandMatcher;
import pl.thedeem.intellij.dpl.inspections.fixes.DropMatchersQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLCommandExpression;
import pl.thedeem.intellij.dpl.psi.DPLCommandMatchersContent;
import pl.thedeem.intellij.dpl.psi.DPLExpressionMatchersList;
import pl.thedeem.intellij.dpl.psi.DPLMembersListMatchers;

public class InvalidMembersListMatcherInspection extends AbstractInvalidMatcherInspection {
    protected void validateMatcher(@NotNull DPLCommandMatchersContent definedMatchers, @NotNull DPLCommandExpression command, @NotNull CommandMatcher matchersDefinition, @NotNull ProblemsHolder holder) {
        if (!"members_list".equals(matchersDefinition.type())) {
            return;
        }
        boolean membersList = definedMatchers instanceof DPLMembersListMatchers;
        boolean singularExpression = definedMatchers instanceof DPLExpressionMatchersList list && list.getExpressionDefinitionList().size() == 1;
        if (!membersList && !singularExpression) {
            holder.registerProblem(
                    definedMatchers,
                    DPLBundle.message("inspection.command.expectedMembersListMatchers", command.getName()),
                    new DropMatchersQuickFix()
            );
        }
    }
}

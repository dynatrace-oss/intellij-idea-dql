package pl.thedeem.intellij.dql.inspections.expressions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.definition.model.MappedParameter;
import pl.thedeem.intellij.dql.inspections.fixes.FlipConditionQuickFix;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLUnaryExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class NegatedFilteringConditionInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitCommand(@NotNull DQLCommand command) {
                super.visitCommand(command);

                Command definition = command.getDefinition();
                if (definition == null || !DQLDefinitionService.FILTER_COMMAND_NEGATIONS.containsKey(definition.name())) {
                    return;
                }
                MappedParameter condition = command.findParameter("condition");
                if (condition == null) {
                    return;
                }
                boolean negativeFound = condition.getExpressions().stream().allMatch(e -> DQLUtil.unpackParenthesis(e) instanceof DQLUnaryExpression);
                if (negativeFound) {
                    holder.registerProblem(
                            command.getCommandKeyword(),
                            DQLBundle.message(
                                    "inspection.negatedFilteringCondition.shouldBeFlipped",
                                    definition.name(),
                                    DQLDefinitionService.FILTER_COMMAND_NEGATIONS.get(definition.name())
                            ),
                            new FlipConditionQuickFix()
                    );
                }
            }
        };
    }
}

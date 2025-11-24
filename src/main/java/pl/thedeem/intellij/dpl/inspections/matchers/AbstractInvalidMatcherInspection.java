package pl.thedeem.intellij.dpl.inspections.matchers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.definition.model.Command;
import pl.thedeem.intellij.dpl.definition.model.CommandMatcher;
import pl.thedeem.intellij.dpl.psi.*;

public abstract class AbstractInvalidMatcherInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitCommandExpression(@NotNull DPLCommandExpression command) {
                super.visitCommandExpression(command);

                Command definition = command.getDefinition();
                DPLCommandMatchers matchers = command.getCommandMatchers();

                if (definition == null || definition.matchers() == null || matchers == null || matchers.getCommandMatchersContent() == null) {
                    return;
                }
                DPLCommandMatchersContent definedMatchers = matchers.getCommandMatchersContent();
                CommandMatcher matchersDefinition = definition.matchers();

                validateMatcher(definedMatchers, command, matchersDefinition, holder);
            }
        };
    }

    protected abstract void validateMatcher(@NotNull DPLCommandMatchersContent definedMatchers, @NotNull DPLCommandExpression command, @NotNull CommandMatcher matchersDefinition, @NotNull ProblemsHolder holder);
}

package pl.thedeem.intellij.dpl.inspections.matchers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.definition.model.CommandMatcher;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.psi.DPLCommandMatchersContent;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLMatchersExpression;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public abstract class AbstractInvalidMatcherInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitExpressionDefinition(@NotNull DPLExpressionDefinition expression) {
                super.visitExpressionDefinition(expression);

                DPLMatchersExpression matchers = expression.getMatchers();
                ExpressionDescription definition = expression.getDefinition();

                if (definition == null || definition.matchers() == null || matchers == null || matchers.getCommandMatchersContent() == null) {
                    return;
                }
                DPLCommandMatchersContent definedMatchers = matchers.getCommandMatchersContent();
                CommandMatcher matchersDefinition = definition.matchers();

                validateMatcher(definedMatchers, expression, matchersDefinition, holder);
            }
        };
    }

    protected abstract void validateMatcher(@NotNull DPLCommandMatchersContent definedMatchers, @NotNull DPLExpressionDefinition expression, @NotNull CommandMatcher matchersDefinition, @NotNull ProblemsHolder holder);
}

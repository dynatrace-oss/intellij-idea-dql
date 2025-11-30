package pl.thedeem.intellij.dpl.inspections.matchers;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.DPLBundle;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;
import pl.thedeem.intellij.dpl.inspections.fixes.DropMatchersQuickFix;
import pl.thedeem.intellij.dpl.psi.DPLExpressionDefinition;
import pl.thedeem.intellij.dpl.psi.DPLMatchersExpression;
import pl.thedeem.intellij.dpl.psi.DPLVisitor;

public class MatchersNotSupportedInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DPLVisitor() {
            @Override
            public void visitExpressionDefinition(@NotNull DPLExpressionDefinition expression) {
                super.visitExpressionDefinition(expression);

                ExpressionDescription definition = expression.getDefinition();
                if (definition == null) {
                    return;
                }

                DPLMatchersExpression matchers = expression.getMatchers();
                if (matchers == null) {
                    return;
                }

                if (definition.matchers() == null) {
                    holder.registerProblem(
                            matchers,
                            DPLBundle.message("inspection.command.matchersNotAllowed"),
                            new DropMatchersQuickFix()
                    );
                }
            }
        };
    }
}

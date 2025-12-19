package pl.thedeem.intellij.dql.inspections.functions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLFunctionExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class UnknownFunctionInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitFunctionExpression(@NotNull DQLFunctionExpression function) {
                super.visitFunctionExpression(function);
                if (function.getDefinition() == null) {
                    holder.registerProblem(
                            function.getFunctionName(),
                            DQLBundle.message("inspection.function.name.unknown"),
                            ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
                    );
                }
            }
        };
    }
}

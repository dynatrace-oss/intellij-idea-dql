package pl.thedeem.intellij.dql.inspections.functions;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLFunctionCallExpression;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class UnknownFunctionInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitFunctionCallExpression(@NotNull DQLFunctionCallExpression function) {
                super.visitFunctionCallExpression(function);
                if (function.getDefinition() == null) {
                    holder.registerProblem(
                            function.getFunctionName(),
                            DQLBundle.message("inspection.function.name.unknown")
                    );
                }
            }
        };
    }
}

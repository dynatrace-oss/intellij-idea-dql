package pl.thedeem.intellij.dql.inspections.simple;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.inspections.fixes.ConvertSingleQuotesToDoubleQuotesQuickFix;
import pl.thedeem.intellij.dql.psi.DQLSingleQuotedString;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class SingleQuotesUsageInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitSingleQuotedString(@NotNull DQLSingleQuotedString string) {
                super.visitSingleQuotedString(string);
                holder.registerProblem(
                        string,
                        DQLBundle.message("inspection.singleQuotes.notSupported"),
                        new ConvertSingleQuotesToDoubleQuotesQuickFix()
                );
            }
        };
    }
}

package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class UnknownCommandInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitQueryStatement(@NotNull DQLQueryStatement command) {
                super.visitQueryStatement(command);
                if (command.getDefinition() == null) {
                    holder.registerProblem(
                            command.getQueryStatementKeyword(),
                            DQLBundle.message("inspection.command.name.unknown", command.getName())
                    );
                }
            }
        };
    }
}

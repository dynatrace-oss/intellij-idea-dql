package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.DQLCommandDefinition;
import pl.thedeem.intellij.dql.definition.DQLCommandGroup;
import pl.thedeem.intellij.dql.psi.DQLQueryStatement;
import pl.thedeem.intellij.dql.psi.DQLQueryStatementKeyword;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class InvalidCommandContextInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitQueryStatement(@NotNull DQLQueryStatement command) {
                super.visitQueryStatement(command);

                DQLCommandDefinition definition = command.getDefinition();
                if (definition == null || DQLUtil.isPartialFile(command.getContainingFile())) {
                    return;
                }
                PsiElement pipe = command.getPipe();
                boolean isStartingCommand = DQLCommandGroup.STARTING_COMMAND_TYPES.contains(definition.getCommandGroup());
                DQLQueryStatementKeyword keyword = command.getQueryStatementKeyword();
                if (command.isFirstStatement()) {
                    if (!isStartingCommand) {
                        holder.registerProblem(
                                keyword,
                                DQLBundle.message("inspection.command.context.invalidStartingCommand", command.getName())
                        );
                    }
                    if (pipe != null) {
                        holder.registerProblem(
                                keyword,
                                DQLBundle.message("inspection.command.context.pipeNotAllowed", command.getName())
                        );
                    }
                } else {
                    if (isStartingCommand) {
                        holder.registerProblem(
                                keyword,
                                DQLBundle.message("inspection.command.context.invalidExtensionCommand", command.getName())
                        );
                    }
                    if (pipe == null) {
                        holder.registerProblem(
                                keyword,
                                DQLBundle.message("inspection.command.context.missingPipe", keyword.getName())
                        );
                    }
                }
            }
        };
    }
}

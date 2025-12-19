package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLCommandKeyword;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

public class InvalidCommandContextInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new DQLVisitor() {
            @Override
            public void visitCommand(@NotNull DQLCommand command) {
                super.visitCommand(command);

                Command definition = command.getDefinition();
                if (definition == null || DQLUtil.isPartialFile(command.getContainingFile())) {
                    return;
                }
                PsiElement pipe = command.getPipe();

                boolean isStartingCommand = "data_source".equals(definition.category());
                DQLCommandKeyword keyword = command.getCommandKeyword();
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

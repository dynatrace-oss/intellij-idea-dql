package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.model.Command;
import pl.thedeem.intellij.dql.inspections.fixes.DropCommandQuickFix;
import pl.thedeem.intellij.dql.inspections.fixes.RenameFileQuickFix;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLCommandKeyword;
import pl.thedeem.intellij.dql.psi.DQLVisitor;

import java.util.ArrayList;
import java.util.List;

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
                boolean canBeFixed = InjectedLanguageManager.getInstance(command.getProject()).getInjectionHost(command.getContainingFile()) == null;
                List<LocalQuickFix> fixes = new ArrayList<>();
                if (canBeFixed) {
                    fixes.add(new RenameFileQuickFix(proposePartialName(command.getContainingFile().getName())));
                }
                fixes.add(new DropCommandQuickFix());
                boolean isStartingCommand = "data_source".equals(definition.category());
                DQLCommandKeyword keyword = command.getCommandKeyword();
                if (command.isFirstStatement()) {
                    if (!isStartingCommand) {
                        holder.registerProblem(
                                keyword,
                                DQLBundle.message("inspection.command.context.invalidStartingCommand", command.getName()),
                                fixes.toArray(new LocalQuickFix[0])
                        );
                    }
                    if (pipe != null) {
                        holder.registerProblem(
                                keyword,
                                DQLBundle.message("inspection.command.context.pipeNotAllowed", command.getName()),
                                fixes.toArray(new LocalQuickFix[0])
                        );
                    }
                } else {
                    if (isStartingCommand) {
                        holder.registerProblem(
                                keyword,
                                DQLBundle.message("inspection.command.context.invalidExtensionCommand", command.getName()),
                                fixes.toArray(new LocalQuickFix[0])
                        );
                    }
                    if (pipe == null) {
                        holder.registerProblem(
                                keyword,
                                DQLBundle.message("inspection.command.context.missingPipe", keyword.getName()),
                                fixes.toArray(new LocalQuickFix[0])
                        );
                    }
                }
            }
        };
    }

    private static @NotNull String proposePartialName(@NotNull String name) {
        if (StringUtil.endsWithIgnoreCase(name, ".dqlpart")) {
            return name;
        }
        if (StringUtil.endsWithIgnoreCase(name, ".dql")) {
            return name + "part";
        }
        return name + ".dqlpart";
    }
}

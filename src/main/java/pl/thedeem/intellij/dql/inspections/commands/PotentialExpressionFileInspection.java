package pl.thedeem.intellij.dql.inspections.commands;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFile;
import pl.thedeem.intellij.dql.inspections.fixes.RenameFileQuickFix;
import pl.thedeem.intellij.dql.psi.DQLCommand;
import pl.thedeem.intellij.dql.psi.DQLQuery;
import pl.thedeem.intellij.dqlexpr.DQLExprLanguage;

public class PotentialExpressionFileInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                super.visitFile(file);

                if (!(file instanceof DQLFile)) {
                    return;
                }

                PsiElement content = findPotentialDQLExpressionContent(file);
                if (content != null && isValidExpressionFile(file)) {
                    holder.registerProblem(
                            content,
                            DQLBundle.message("inspection.potentialExpressionFile.detected"),
                            ProblemHighlightType.WARNING,
                            new RenameFileQuickFix(file.getName().replaceFirst("(?i)\\.dql$", ".dqlexpr"))
                    );
                }
            }
        };
    }

    private @Nullable PsiElement findPotentialDQLExpressionContent(@NotNull PsiFile file) {
        DQLQuery query = PsiTreeUtil.getChildOfType(file, DQLQuery.class);
        if (query == null) {
            return PsiTreeUtil.getChildOfType(file, PsiErrorElement.class);
        }
        DQLCommand[] commands = PsiTreeUtil.getChildrenOfType(query, DQLCommand.class);
        if (commands == null || commands.length != 1) {
            return null;
        }
        DQLCommand command = commands[0];
        if (command.getDefinition() != null) {
            return null;
        }

        return command;
    }

    private boolean isValidExpressionFile(@NotNull PsiFile file) {
        try {
            PsiFile dqlExprFile = PsiFileFactory.getInstance(file.getProject())
                    .createFileFromText("temp.dqlexpr", DQLExprLanguage.INSTANCE, file.getText());
            PsiErrorElement[] errors = PsiTreeUtil.getChildrenOfType(dqlExprFile, PsiErrorElement.class);
            return errors == null || errors.length == 0;
        } catch (Exception e) {
            return false;
        }
    }
}


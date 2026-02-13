package pl.thedeem.intellij.dql.inspections.fixes;

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.rename.RenameProcessor;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFileType;

public class RenameFileQuickFix implements LocalQuickFix {
    private final String proposedName;

    public RenameFileQuickFix(@NotNull String proposedName) {
        this.proposedName = proposedName;
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull ProblemDescriptor previewDescriptor) {
        return IntentionPreviewInfo.EMPTY;
    }

    @Override
    public @NotNull String getFamilyName() {
        return DQLBundle.message("inspection.command.context.fixes.renameFile", proposedName);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement element = descriptor.getPsiElement();
        if (element == null) {
            return;
        }

        PsiFile file = element.getContainingFile();
        if (file == null || !DQLFileType.INSTANCE.equals(file.getFileType())) {
            return;
        }

        new RenameProcessor(project, file, proposedName, true, true).run();
    }
}

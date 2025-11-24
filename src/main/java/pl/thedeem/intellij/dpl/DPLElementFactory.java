package pl.thedeem.intellij.dpl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.psi.DPLFieldName;
import pl.thedeem.intellij.dpl.psi.DPLVariable;

import java.util.Objects;

public class DPLElementFactory {
    public static @NotNull DPLFieldName createFieldName(String name, Project project) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText(
                "temporary.dpl",
                DynatracePatternLanguage.INSTANCE,
                "INT:" + name
        );
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, DPLFieldName.class));
    }

    public static DPLVariable createVariable(@NotNull String newName, @NotNull Project project) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText(
                "temporary.dpl",
                DynatracePatternLanguage.INSTANCE,
                newName
        );
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, DPLVariable.class));
    }
}

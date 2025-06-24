package pl.thedeem.intellij.dql.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DQLElementFactory {
    public static @NotNull DQLFieldExpression createFieldElement(String name, Project project) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "fetch " + name);
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, DQLFieldExpression.class));
    }

    public static DQLVariableExpression createVariableElement(@NotNull String newName, @NotNull Project project) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "fetch " + newName);
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, DQLVariableExpression.class));
    }

    public static PsiElement createUnknownElement(@NotNull Project project, @NotNull String text) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "fetch " + text);
        DQLQueryStatementKeyword keyword = PsiTreeUtil.findChildOfType(newFile, DQLQueryStatementKeyword.class);
        if (keyword == null) {
            throw new RuntimeException("DQLElementFactory::createUnknownElement error");
        }
        return keyword.getNextSibling().getNextSibling();
    }
}

package pl.thedeem.intellij.dql.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;

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
        return Objects.requireNonNull(keyword).getNextSibling().getNextSibling();
    }

    public static @NotNull PsiComment createInlineComment(@NotNull Project project, @NotNull String text) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "//" + text);
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, PsiComment.class));
    }

    public static @NotNull PsiComment createMultiLineComment(@NotNull Project project, @NotNull String text) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "/*" + text + "*/");
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, PsiComment.class));
    }
}

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
    public static @NotNull DQLFieldExpression createFieldElement(@NotNull String name, @NotNull Project project) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "fetch " + name);
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, DQLFieldExpression.class));
    }

    public static DQLVariableExpression createVariableElement(@NotNull String newName, @NotNull Project project) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "fetch " + newName);
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, DQLVariableExpression.class));
    }

    public static @NotNull PsiElement createUnknownElement(@NotNull Project project, @NotNull String text) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "fetch " + text);
        DQLCommandKeyword keyword = PsiTreeUtil.findChildOfType(newFile, DQLCommandKeyword.class);
        if (keyword == null || keyword.getNextSibling() == null) {
            return createMultiLineComment(project, "an empty element");
        }
        return keyword.getNextSibling().getNextSibling();
    }

    public static @NotNull PsiComment createInlineComment(@NotNull Project project, @NotNull String text) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "//" + text);
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, PsiComment.class));
    }

    public static @NotNull PsiComment createMultiLineComment(@NotNull Project project, @NotNull String text) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "/*" + text + "*/");
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, PsiComment.class));
    }

    public static @NotNull DQLDoubleQuotedString createStringElement(@NotNull Project project, @NotNull String text) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "fetch " + "\"" + text + "\"");
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, DQLDoubleQuotedString.class));
    }

    public static @NotNull DQLMultilineString createStringBlockElement(@NotNull Project project, @NotNull String text) {
        PsiFile newFile = PsiFileFactory.getInstance(project).createFileFromText("temporary.dql", DynatraceQueryLanguage.INSTANCE, "fetch " + "\"\"\"" + text + "\"\"\"");
        return Objects.requireNonNull(PsiTreeUtil.findChildOfType(newFile, DQLMultilineString.class));
    }
}

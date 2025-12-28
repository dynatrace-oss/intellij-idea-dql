package pl.thedeem.intellij.dql.actions;

import com.intellij.lang.Language;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.services.ui.DQLManagedService;

import java.util.List;

public abstract class ActionUtils {
    public static @Nullable DQLManagedService<?> getService(@NotNull AnActionEvent e) {
        return e.getData(DQLManagedService.EXECUTION_SERVICE);
    }

    public static <T extends DQLManagedService<?>> @Nullable T getService(@NotNull AnActionEvent e, Class<T> serviceClass) {
        DQLManagedService<?> serviceKey = e.getData(DQLManagedService.EXECUTION_SERVICE);
        if (!(serviceClass.isInstance(serviceKey))) {
            return null;
        }

        return serviceClass.cast(serviceKey);
    }

    public static @Nullable PsiFile getRelatedPsiFile(@Nullable PsiFile originalFile, @Nullable Editor editor, @Nullable PsiElement psiElement) {
        if (originalFile == null || editor == null) {
            return null;
        }
        if (!DQLFileType.INSTANCE.equals(originalFile.getFileType())) {
            InjectedLanguageManager manager = InjectedLanguageManager.getInstance(originalFile.getProject());
            PsiElement injectedElement = manager.findInjectedElementAt(originalFile, editor.getCaretModel().getOffset());
            if (injectedElement != null && DQLFileType.INSTANCE.equals(injectedElement.getContainingFile().getFileType())) {
                return injectedElement.getContainingFile();
            } else if (psiElement != null) {
                return DQLFileType.INSTANCE.equals(psiElement.getContainingFile().getFileType()) ? psiElement.getContainingFile() : null;
            }
        }
        return originalFile;
    }

    public static boolean isNotRelatedToDQL(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null) {
            return true;
        }
        if (DQLFileType.INSTANCE.equals(file.getFileType())) {
            return false;
        }
        Language[] langs = e.getData(LangDataKeys.CONTEXT_LANGUAGES);
        return langs == null || !List.of(langs).contains(DynatraceQueryLanguage.INSTANCE);
    }

    public static @NotNull String generateServiceName(@NotNull PsiFile file) {
        return file.getName();
    }

    public static @Nullable Editor getEditor(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            FileEditor fileEditor = e.getData(PlatformCoreDataKeys.FILE_EDITOR);
            editor = fileEditor instanceof TextEditor textEditor ? textEditor.getEditor() : null;
        }
        return editor;
    }
}

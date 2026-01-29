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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.psi.DQLQuery;
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

    public static @Nullable PsiFile getRelatedPsiFile(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return null;
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            if (virtualFile != null) {
                psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            }

        }
        if (psiFile == null) {
            return null;
        }
        if (!DQLFileType.INSTANCE.equals(psiFile.getFileType())) {
            InjectedLanguageManager manager = InjectedLanguageManager.getInstance(psiFile.getProject());
            Editor editor = ActionUtils.getEditor(e);
            if (editor == null) {
                return null;
            }
            PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
            PsiElement injectedElement = manager.findInjectedElementAt(psiFile, editor.getCaretModel().getOffset());
            if (injectedElement != null && DQLFileType.INSTANCE.equals(injectedElement.getContainingFile().getFileType())) {
                return injectedElement.getContainingFile();
            } else if (psiElement != null) {
                return DQLFileType.INSTANCE.equals(psiElement.getContainingFile().getFileType()) ? psiElement.getContainingFile() : null;
            }
        }
        return psiFile;
    }

    public static boolean isNotRelatedToDQL(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null) {
            return true;
        }
        if (DQLFileType.INSTANCE.equals(file.getFileType())) {
            return false;
        }
        if (PsiTreeUtil.getChildrenOfType(file, DQLQuery.class) == null) {
            return true;
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

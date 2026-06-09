package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;

import javax.swing.*;
import java.util.Objects;

public abstract class DQLVirtualFile<T> extends LightVirtualFile {
    protected final T content;
    private @Nullable TextEditor textEditor = null;

    public DQLVirtualFile(@NotNull String name, @NotNull T content) {
        super(name, DQLFileType.INSTANCE, content.toString());
        this.content = content;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DQLVirtualFile<?> casted = this.getClass().cast(o);
        return Objects.equals(content, casted.content) && Objects.equals(getName(), casted.getName());
    }

    @Override
    public int hashCode() {
        return content.hashCode();
    }

    public void dispose() {
        if (textEditor != null) {
            TextEditorProvider.getInstance().disposeEditor(textEditor);
            textEditor = null;
        }
    }

    public @NotNull JComponent createComponent(@NotNull Project project) {
        dispose();

        FileType baseFileType = getBaseFileType();
        Language language = baseFileType instanceof LanguageFileType lft ? lft.getLanguage() : PlainTextLanguage.INSTANCE;
        PsiFile psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText(getName(), language, getDocumentContent());
        VirtualFile vf = psiFile.getVirtualFile();
        if (vf == null) {
            return JBUI.Panels.simplePanel();
        }

        FileEditor fileEditor = TextEditorProvider.getInstance().createEditor(project, vf);
        if (!(fileEditor instanceof TextEditor te)) {
            fileEditor.dispose();
            return JBUI.Panels.simplePanel();
        }
        if (te.getEditor() instanceof EditorEx editorEx) {
            editorEx.setViewer(true);
        }
        this.textEditor = te;
        return te.getComponent();
    }

    protected @NotNull FileType getBaseFileType() {
        return PlainTextFileType.INSTANCE;
    }

    protected @NotNull String getDocumentContent() {
        return content.toString();
    }
}

package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.ex.EditorEx;
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
        FileType baseFileType = getBaseFileType();
        Language language = baseFileType instanceof LanguageFileType lft ? lft.getLanguage() : PlainTextLanguage.INSTANCE;
        PsiFile psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText(getName(), language, getDocumentContent());
        VirtualFile vf = Objects.requireNonNull(psiFile.getVirtualFile());
        this.textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, vf);
        ((EditorEx) this.textEditor.getEditor()).setViewer(true);
        return this.textEditor.getComponent();
    }

    protected @NotNull FileType getBaseFileType() {
        return PlainTextFileType.INSTANCE;
    }

    protected @NotNull String getDocumentContent() {
        return content.toString();
    }
}

package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLFileType;

import javax.swing.*;
import java.util.Objects;

public abstract class DQLVirtualFile<T> extends LightVirtualFile {
    protected final T content;
    protected Editor editor = null;

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
        if (editor != null) {
            EditorFactory.getInstance().releaseEditor(editor);
        }
    }

    public @NotNull JComponent createComponent(@NotNull Project project) {
        LightVirtualFile displayFile = new LightVirtualFile(getName(), getBaseFileType(), getDocumentContent());
        displayFile.setWritable(false);
        Document document = Objects.requireNonNull(FileDocumentManager.getInstance().getDocument(displayFile));
        this.editor = EditorFactory.getInstance().createEditor(document, project, getBaseFileType(), true);
        EditorEx editorEx = (EditorEx) this.editor;
        editorEx.setFile(displayFile);
        editorEx.getSettings().setFoldingOutlineShown(true);
        editorEx.getFoldingModel().setFoldingEnabled(true);
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!editor.isDisposed()) {
                CodeFoldingManager.getInstance(project).updateFoldRegionsAsync(editor, true);
            }
        });
        return this.editor.getComponent();
    }

    protected @NotNull FileType getBaseFileType() {
        return PlainTextFileType.INSTANCE;
    }

    protected @NotNull String getDocumentContent() {
        return content.toString();
    }
}

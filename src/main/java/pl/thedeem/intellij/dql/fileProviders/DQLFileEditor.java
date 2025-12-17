package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class DQLFileEditor extends UserDataHolderBase implements FileEditor {
    private final VirtualFile virtualFile;
    private final JComponent component;

    public DQLFileEditor(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        this.virtualFile = virtualFile;
        if (virtualFile instanceof DQLVirtualFile<?> file) {
            this.component = file.createComponent(project);
        } else {
            this.component = new JPanel();
        }
    }

    @Override
    public @NotNull JComponent getComponent() {
        return component;
    }

    @Override
    public @Nullable JComponent getPreferredFocusedComponent() {
        return component;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName() {
        return virtualFile.getName();
    }

    @Override
    public void setState(@NotNull FileEditorState fileEditorState) {
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public boolean isValid() {
        return virtualFile instanceof DQLVirtualFile;
    }

    @Override
    public void addPropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
    }

    @Override
    public void removePropertyChangeListener(@NotNull PropertyChangeListener propertyChangeListener) {
    }

    @Override
    public void dispose() {
        if (virtualFile instanceof DQLVirtualFile<?> file) {
            file.dispose();
        }
    }

    @Override
    @NotNull
    public VirtualFile getFile() {
        return virtualFile;
    }
}

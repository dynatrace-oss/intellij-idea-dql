package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.exec.panel.DQLQueryConsolePanel;

import javax.swing.*;

public class DQLQueryConsoleVirtualFile extends DQLVirtualFile<String> {
    private final String initialTenant;

    public DQLQueryConsoleVirtualFile(@NotNull String name, @Nullable String initialTenant) {
        super(name, "");
        this.initialTenant = initialTenant;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    protected @NotNull FileType getBaseFileType() {
        return DQLFileType.INSTANCE;
    }

    @Override
    public @NotNull JComponent createComponent(@NotNull Project project) {
        return new DQLQueryConsolePanel(project, content, this, initialTenant);
    }
}

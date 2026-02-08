package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.exec.panel.DQLQueryConsolePanel;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class DQLQueryConsoleVirtualFile extends DQLVirtualFile<String> {
    public static final AtomicInteger COUNTER = new AtomicInteger(0);
    private String initialTenant;

    public DQLQueryConsoleVirtualFile(@NotNull String name) {
        super(name, "");
    }

    public DQLQueryConsoleVirtualFile(@NotNull String name, @NotNull String content) {
        super(name, content);
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

    public @NotNull DQLQueryConsoleVirtualFile setInitialTenant(@Nullable String initialTenant) {
        this.initialTenant = initialTenant;
        return this;
    }

    public static void open(@NotNull Project project) {
        open(project, DQLBundle.message("editor.queryConsole.consoleName", COUNTER.incrementAndGet()), null);
    }

    public static void openForTenant(@NotNull Project project, @Nullable String tenant) {
        open(project, DQLBundle.message("editor.queryConsole.consoleName", COUNTER.incrementAndGet()), tenant);
    }

    public static void open(@NotNull Project project, @NotNull String name, @Nullable String initialTenant) {
        DQLQueryConsoleVirtualFile vf = new DQLQueryConsoleVirtualFile(name)
                .setInitialTenant(initialTenant);
        FileEditorManager.getInstance(project).openFile(vf, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DQLVirtualFile<?> casted = this.getClass().cast(o);
        return Objects.equals(getName(), casted.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}

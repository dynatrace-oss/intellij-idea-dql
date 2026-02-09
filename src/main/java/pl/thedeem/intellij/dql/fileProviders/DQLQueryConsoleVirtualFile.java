package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.panel.DQLQueryConsolePanel;

import javax.swing.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class DQLQueryConsoleVirtualFile extends DQLVirtualFile<String> {
    public static final AtomicInteger COUNTER = new AtomicInteger(0);
    private QueryConfiguration initialConfiguration;

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
        return new DQLQueryConsolePanel(project, content, this, initialConfiguration);
    }

    public @NotNull DQLQueryConsoleVirtualFile setInitialConfiguration(@Nullable QueryConfiguration initialConfiguration) {
        this.initialConfiguration = initialConfiguration;
        return this;
    }

    public static void open(@NotNull Project project) {
        open(project, DQLBundle.message("editor.queryConsole.consoleName", COUNTER.incrementAndGet()), null);
    }

    public static void openForTenant(@NotNull Project project, @Nullable QueryConfiguration configuration) {
        open(project, DQLBundle.message("editor.queryConsole.consoleName", COUNTER.incrementAndGet()), configuration);
    }

    public static void open(@NotNull Project project, @NotNull String name, @Nullable QueryConfiguration configuration) {
        DQLQueryConsoleVirtualFile vf = new DQLQueryConsoleVirtualFile(name)
                .setInitialConfiguration(configuration);
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

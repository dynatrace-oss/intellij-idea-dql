package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.exec.panel.DQLRecordPanel;

import javax.swing.*;
import java.util.Map;

public class DQLRecordVirtualFile extends DQLVirtualFile<Map<String, Object>> {
    public DQLRecordVirtualFile(@NotNull String name, @NotNull Map<String, Object> content) {
        super(name, content);
    }

    @Override
    public @NotNull JComponent createComponent(@NotNull Project project) {
        return new DQLRecordPanel(content);
    }
}

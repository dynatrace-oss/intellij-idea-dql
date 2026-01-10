package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.components.execution.DQLMetadataPanel;

import javax.swing.*;
import java.time.ZonedDateTime;

public class DQLMetadataVirtualFile extends DQLVirtualFile<DQLResult.DQLGrailMetadata> {
    private final ZonedDateTime executionTime;

    public DQLMetadataVirtualFile(@NotNull String name, @NotNull DQLResult.DQLGrailMetadata content, @NotNull ZonedDateTime executionTime) {
        super(name, content);
        this.executionTime = executionTime;
    }

    @Override
    public @NotNull JComponent createComponent(@NotNull Project project) {
        return new DQLMetadataPanel(content, executionTime);
    }
}

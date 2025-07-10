package pl.thedeem.intellij.dql.fileProviders;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.components.DQLMetadataPanel;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;

import javax.swing.*;
import java.time.LocalDateTime;

public class DQLMetadataVirtualFile extends DQLVirtualFile<DQLResult.DQLGrailMetadata> {
   private final LocalDateTime executionTime;

   public DQLMetadataVirtualFile(@NotNull String name, @NotNull DQLResult.DQLGrailMetadata content, @NotNull LocalDateTime executionTime) {
      super(name, content);
      this.executionTime = executionTime;
   }

   @Override
   public @NotNull JComponent createComponent(@NotNull Project project) {
      return new DQLMetadataPanel(content, executionTime);
   }
}

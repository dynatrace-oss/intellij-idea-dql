package pl.thedeem.intellij.dql.components;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.components.execution.DQLExecutionTablePanel;
import pl.thedeem.intellij.dql.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.dql.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.sdk.model.errors.DQLAuthErrorResponse;
import pl.thedeem.intellij.dql.sdk.model.errors.DQLErrorResponse;
import pl.thedeem.intellij.dql.sdk.model.errors.DQLExecutionErrorResponse;

import javax.swing.*;
import java.awt.*;

public class DQLResultPanel extends JPanel {
   private final JPanel progressPanel;
   private final JProgressBar progressBar;
   private final JPanel resultsPanel;
   private final Project project;

   public DQLResultPanel(@NotNull Project project) {
      this.project = project;
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder());

      progressBar = new JProgressBar();
      progressBar.setString(DQLBundle.message("runConfiguration.executeDQL.panel.progress.start"));
      progressBar.setStringPainted(true);
      progressPanel = new JPanel(new GridBagLayout());
      progressPanel.add(progressBar);
      progressBar.setIndeterminate(true);
      resultsPanel = new JPanel(new BorderLayout());
   }

   public void registerProgress(@NotNull DQLExecuteResponse executedResponse) {
      progressBar.setIndeterminate(false);
      removeAll();
      add(progressPanel, BorderLayout.CENTER);

      updateProgressBar(0, executedResponse.getState());
      revalidate();
      repaint();
   }

   public void registerProgress(@NotNull DQLPollResponse dqlResponse) {
      progressBar.setIndeterminate(false);
      updateProgressBar(dqlResponse.progress != null ? dqlResponse.progress.intValue() : 0, dqlResponse.state);
      if (dqlResponse.isFinished()) {
         resultsPanel.removeAll();
         if (dqlResponse.getResult() != null && !dqlResponse.getResult().getRecords().isEmpty()) {
            resultsPanel.add(new DQLExecutionTablePanel(project, dqlResponse.result), BorderLayout.CENTER);
         }
         else {
            resultsPanel.add(new JBScrollPane(createInformationComponent(
                DQLBundle.message("runConfiguration.executeDQL.infos.emptyRecords"),
                AllIcons.General.Information
            )), BorderLayout.CENTER);
         }
         removeAll();
         add(resultsPanel, BorderLayout.CENTER);
      }
      revalidate();
      repaint();
   }

   public void updateProgressBar(int progress, String state) {
      progressBar.setValue(progress);
      progressBar.setString(state);
   }

   public void showError(@Nullable DQLErrorResponse<?> response, Exception e) {
      String message = DQLBundle.message("runConfiguration.executeDQL.errors.unknown", e.getMessage());
      if (response != null) {
         if (response.error instanceof DQLAuthErrorResponse error) {
            String details = error.message;
            if (error.details.get("errorMessage") instanceof String msg) {
               details = msg;
            }
            message = DQLBundle.message("runConfiguration.executeDQL.errors.unauthorized", details);
         } else if (response.error instanceof DQLExecutionErrorResponse error) {
            String details = error.message;
            if (error.details.errorMessage instanceof String msg) {
               details = msg;
            }
            message = DQLBundle.message("runConfiguration.executeDQL.errors.execution", details);
         }
      } else if (e instanceof InterruptedException) {
         message = DQLBundle.message("runConfiguration.executeDQL.indicator.cancelled", e.getMessage());
      }

      resultsPanel.removeAll();
      resultsPanel.add(new JBScrollPane(createInformationComponent(message, AllIcons.General.Error)), BorderLayout.CENTER);
      removeAll();
      add(resultsPanel, BorderLayout.CENTER);
   }

   private JComponent createInformationComponent(@NotNull String message, @NotNull Icon icon) {
      JPanel panel = new JPanel(new GridBagLayout());
      panel.setBorder(BorderFactory.createEmptyBorder());
      panel.add(new JLabel(message, icon, JLabel.LEFT));
      return panel;
   }
}

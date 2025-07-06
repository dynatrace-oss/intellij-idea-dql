package pl.thedeem.intellij.dql.components.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.executing.executeDql.DQLExecutionService;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class OpenQueryMetadataAction extends AnAction {
   private final DQLExecutionService service;

   public OpenQueryMetadataAction(@Nullable String text, @Nullable String description, @NotNull DQLExecutionService service) {
      super(text, description, AllIcons.Nodes.DataTables);
      this.service = service;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void actionPerformed(@NotNull AnActionEvent e) {
      DQLResult result = service.getResult();
      if (result == null) {
         return;
      }
      DQLResult.DQLGrailMetadata metadata = result.getGrailMetadata();
      ColumnInfo<MetadataRow, Object>[] columnInfos = new ColumnInfo[2];

      columnInfos[0] = new ColumnInfo<>(DQLBundle.message("components.queryDetails.columns.property")) {
         @Override
         public @Nullable Object valueOf(MetadataRow s) {
            return s.property();
         }
      };
      columnInfos[1] = new ColumnInfo<>(DQLBundle.message("components.queryDetails.columns.value")) {
         @Override
         public @Nullable Object valueOf(MetadataRow s) {
            return s.value();
         }
      };

      if (metadata != null) {
         JBScrollPane scroll = prepareResultPanel(columnInfos, metadata, service);
         scroll.setPreferredSize(new Dimension(600, 400));
         JOptionPane.showMessageDialog(
             null,
             scroll,
             DQLBundle.message("components.tableResults.cellDetails.title", service.getName()),
             JOptionPane.INFORMATION_MESSAGE,
             AllIcons.Nodes.DataTables
         );
      }
   }

   private static @NotNull JBScrollPane prepareResultPanel(ColumnInfo<MetadataRow, Object>[] columnInfos, DQLResult.DQLGrailMetadata metadata, DQLExecutionService service) {
      JBTable tableResults = new JBTable(new ListTableModel<>(columnInfos, List.of(
          new MetadataRow(DQLBundle.message("components.queryDetails.values.executionMoment"), service.getExecutionTime().format(DQLUtil.DQL_DATE_FORMATTER)),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.queryID"), metadata.getQueryId()),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.scannedRecords"), String.valueOf(metadata.getScannedRecords())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.scannedDataPoints"), String.valueOf(metadata.getScannedDataPoints())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.scannedBytes"), String.valueOf(metadata.getScannedBytes())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.executionTime"), String.valueOf(metadata.getExecutionTimeMilliseconds())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.timezone"), String.valueOf(metadata.getTimezone())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.analysisTimeframeStart"), metadata.getAnalysisTimeframeStart()),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.analysisTimeframeEnd"), metadata.getAnalysisTimeframeEnd()),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.sampled"), String.valueOf(metadata.isSampled()))
      ), 0));
      tableResults.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      return new JBScrollPane(tableResults);
   }

   private record MetadataRow(String property, String value) {
   }

   @Override
   public void update(@NotNull AnActionEvent e) {
      Presentation presentation = e.getPresentation();
      presentation.setEnabledAndVisible(service.getResult() != null);
   }

   @Override
   public @NotNull ActionUpdateThread getActionUpdateThread() {
      return ActionUpdateThread.EDT;
   }
}

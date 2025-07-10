package pl.thedeem.intellij.dql.components;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.components.common.CommonTable;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DQLMetadataPanel extends JPanel {
   public DQLMetadataPanel(DQLResult.DQLGrailMetadata metadata, @NotNull LocalDateTime executionTime) {
      setLayout(new BorderLayout());
      List<MetadataRow> records = List.of(
          new MetadataRow(DQLBundle.message("components.queryDetails.values.executionMoment"), executionTime.format(DQLUtil.DQL_DATE_FORMATTER)),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.queryID"), metadata.getQueryId()),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.scannedRecords"), String.valueOf(metadata.getScannedRecords())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.scannedDataPoints"), String.valueOf(metadata.getScannedDataPoints())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.scannedBytes"), String.valueOf(metadata.getScannedBytes())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.executionTime"), String.valueOf(metadata.getExecutionTimeMilliseconds())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.timezone"), String.valueOf(metadata.getTimezone())),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.analysisTimeframeStart"), metadata.getAnalysisTimeframeStart()),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.analysisTimeframeEnd"), metadata.getAnalysisTimeframeEnd()),
          new MetadataRow(DQLBundle.message("components.queryDetails.values.sampled"), String.valueOf(metadata.isSampled()))
      );
      List<ColumnInfo<MetadataRow, Object>> columnInfos = calculateColumns();
      CommonTable table = new CommonTable(new ListTableModel<>(columnInfos.toArray(new ColumnInfo[]{}), records, 0));
      table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      JBScrollPane scroll = new JBScrollPane(table);
      table.setColumnPreferredWidthInCharacters(0, 30);
      add(scroll, BorderLayout.CENTER);
   }

   private List<ColumnInfo<MetadataRow, Object>> calculateColumns() {
      List<ColumnInfo<MetadataRow, Object>> result = new ArrayList<>(2);
      result.add(new ColumnInfo<>(DQLBundle.message("components.queryDetails.columns.property")) {
         @Override
         public @Nullable Object valueOf(MetadataRow s) {
            return s.property();
         }
      });
      result.add(new ColumnInfo<>(DQLBundle.message("components.queryDetails.columns.value")) {
         @Override
         public @Nullable Object valueOf(MetadataRow s) {
            return s.value();
         }
      });
      return result;
   }

   private record MetadataRow(String property, String value) {
   }
}

package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.TransparentScrollPane;
import pl.thedeem.intellij.common.components.table.CommonTable;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;

import javax.swing.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DQLMetadataPanel extends BorderLayoutPanel {
    public DQLMetadataPanel(@NotNull DQLResult.DQLGrailMetadata metadata, @Nullable ZonedDateTime executionTime) {
        super();
        setOpaque(false);
        setBorder(JBUI.Borders.empty());
        List<MetadataRow> records = List.of(
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.executedOn"), executionTime != null ? executionTime.format(DQLUtil.USER_FRIENDLY_DATE_FORMATTER) : "-"),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.queryID"), metadata.getQueryId()),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.scannedRecords"), String.valueOf(metadata.getScannedRecords())),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.scannedDataPoints"), String.valueOf(metadata.getScannedDataPoints())),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.scannedBytes"), String.valueOf(metadata.getScannedBytes())),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.executionTime"), String.valueOf(metadata.getExecutionTimeMilliseconds())),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.timezone"), String.valueOf(metadata.getTimezone())),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.analysisTimeframeStart"), metadata.getAnalysisTimeframeStart()),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.analysisTimeframeEnd"), metadata.getAnalysisTimeframeEnd()),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.sampled"), String.valueOf(metadata.isSampled())),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.locale"), String.valueOf(metadata.getLocale())),
                new MetadataRow(DQLBundle.message("components.executionMetadata.data.dqlVersion"), String.valueOf(metadata.getDqlVersion()))
        );
        List<ColumnInfo<MetadataRow, Object>> columnInfos = calculateColumns();
        CommonTable table = new CommonTable(new ListTableModel<>(columnInfos.toArray(new ColumnInfo[]{}), records, 0));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setColumnPreferredWidthInCharacters(0, 30);
        addToCenter(new TransparentScrollPane(table));
    }

    private @NotNull List<ColumnInfo<MetadataRow, Object>> calculateColumns() {
        List<ColumnInfo<MetadataRow, Object>> result = new ArrayList<>(2);
        result.add(new ColumnInfo<>(DQLBundle.message("components.executionMetadata.columns.property")) {
            @Override
            public @Nullable Object valueOf(MetadataRow s) {
                return s.property();
            }
        });
        result.add(new ColumnInfo<>(DQLBundle.message("components.executionMetadata.columns.value")) {
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

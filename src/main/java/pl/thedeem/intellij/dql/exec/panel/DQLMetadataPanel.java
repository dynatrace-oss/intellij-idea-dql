package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.CommonTable;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DQLMetadataPanel extends BorderLayoutPanel {
    public DQLMetadataPanel(DQLResult.DQLGrailMetadata metadata) {
        super();
        setOpaque(false);
        setBorder(JBUI.Borders.empty());
        List<MetadataRow> records = List.of(
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
        addToCenter(scroll);
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

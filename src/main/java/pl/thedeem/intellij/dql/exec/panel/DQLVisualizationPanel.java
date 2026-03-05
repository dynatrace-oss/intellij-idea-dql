package pl.thedeem.intellij.dql.exec.panel;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.Icons;
import pl.thedeem.intellij.common.components.PanelWithToolbarActions;
import pl.thedeem.intellij.common.components.charts.AbstractChartComponent;
import pl.thedeem.intellij.common.components.charts.generators.CategoryChart;
import pl.thedeem.intellij.common.components.charts.generators.ChartDataRecord;
import pl.thedeem.intellij.common.components.charts.generators.PieChart;
import pl.thedeem.intellij.common.components.charts.generators.XYChart;
import pl.thedeem.intellij.common.sdk.model.DQLRecord;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

public class DQLVisualizationPanel extends AbstractChartComponent implements PanelWithToolbarActions {
    private static final Logger logger = Logger.getInstance(DQLVisualizationPanel.class);
    private final List<ChartDataRecord> records;
    private final Map<String, Class<?>> columns;

    private ChartType type = ChartType.BAR;

    public DQLVisualizationPanel(@Nullable DQLResult response) {
        super();
        Map<String, String> declaredColumns = response == null ? Map.of() : new HashMap<>(Objects.requireNonNullElseGet(response.getColumnTypes(), Map::of));
        this.columns = getAvailableColumns(response, declaredColumns);
        this.records = prepareChartRecords(response, declaredColumns);
        showChart(new CategoryChart(this.records, this.columns));
    }

    @Override
    public boolean isDataEmpty() {
        return records.isEmpty();
    }

    public @NotNull AnAction[] getToolbarActions() {
        return List.of(
                new ToggleAction(DQLBundle.message("components.visualization.chartTypes.line"), null, Icons.LINE_CHART) {
                    @Override
                    public boolean isSelected(@NotNull AnActionEvent e) {
                        return type == ChartType.LINE;
                    }

                    @Override
                    public void setSelected(@NotNull AnActionEvent e, boolean selected) {
                        if (selected) {
                            type = ChartType.LINE;
                            showChart(new XYChart(records, columns));
                        }
                    }

                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return ActionUpdateThread.EDT;
                    }
                },
                new ToggleAction(DQLBundle.message("components.visualization.chartTypes.bar"), null, Icons.BAR_CHART) {
                    @Override
                    public boolean isSelected(@NotNull AnActionEvent e) {
                        return type == ChartType.BAR;
                    }

                    @Override
                    public void setSelected(@NotNull AnActionEvent e, boolean selected) {
                        if (selected) {
                            type = ChartType.BAR;
                            showChart(new CategoryChart(records, columns));
                        }
                    }

                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return ActionUpdateThread.EDT;
                    }
                },
                new ToggleAction(DQLBundle.message("components.visualization.chartTypes.pie"), null, Icons.PIE_CHART) {
                    @Override
                    public boolean isSelected(@NotNull AnActionEvent e) {
                        return type == ChartType.PIE;
                    }

                    @Override
                    public void setSelected(@NotNull AnActionEvent e, boolean selected) {
                        if (selected) {
                            type = ChartType.PIE;
                            showChart(new PieChart(records, columns));
                        }
                    }

                    @Override
                    public @NotNull ActionUpdateThread getActionUpdateThread() {
                        return ActionUpdateThread.EDT;
                    }
                }
        ).toArray(new AnAction[0]);
    }

    private static @NotNull Map<String, Class<?>> getAvailableColumns(@Nullable DQLResult response, @NotNull Map<String, String> columns) {
        if (response == null) {
            return Map.of();
        }
        Map<String, Class<?>> result = new HashMap<>();

        for (String column : response.getColumns()) {
            result.put(column, switch (columns.get(column)) {
                case "long" -> Long.class;
                case "double", "duration", "number" -> Double.class;
                case "boolean" -> Boolean.class;
                case "timestamp", "timeframe" -> Instant.class;
                case "array" -> List.class;
                case "string" -> String.class;
                case null, default -> Object.class;
            });
        }

        return result;
    }

    private static @NotNull List<ChartDataRecord> prepareChartRecords(@Nullable DQLResult response, @NotNull Map<String, String> columns) {
        if (response == null) {
            return List.of();
        }
        List<DQLRecord> returned = Objects.requireNonNullElseGet(response.getRecords(), List::of);

        List<ChartDataRecord> result = new ArrayList<>(returned.size());
        for (DQLRecord record : returned) {
            result.add(new DQLRecordChartDataAccessor(record, columns));
        }
        return result;
    }

    private enum ChartType {
        BAR,
        LINE,
        PIE,
        AREA
    }

    private record DQLRecordChartDataAccessor(@NotNull Map<String, Object> record,
                                              @NotNull Map<String, String> columnTypes) implements ChartDataRecord {
        @Override
        public @Nullable Object valueOf(@NotNull String column) {
            return record.get(column);
        }

        @Override
        public @NotNull List<Number> numericValueOf(@NotNull String column) {
            Object dataValue = valueOf(column);
            if (dataValue == null) {
                return List.of();
            }
            return switch (columnTypes.get(column)) {
                case "long" -> List.of(Long.valueOf(dataValue.toString()));
                case "double", "duration", "number" -> List.of(Double.valueOf(dataValue.toString()));
                case "timestamp" -> {
                    Number number = convertTimestamp(dataValue.toString());
                    yield number != null ? List.of(number) : List.of();
                }
                case "timeframe" -> getTimeframeIntervals(dataValue);
                case "array" -> {
                    List<Number> result = new ArrayList<>();
                    if (dataValue instanceof List<?> list) {
                        for (Object o : list) {
                            try {
                                result.add(Double.valueOf(o.toString()));
                            } catch (NumberFormatException | NullPointerException ignored) {
                                result.add(null);
                            }
                        }
                    }
                    yield result;
                }
                case null, default -> List.of();
            };
        }

        @Override
        public @NotNull List<String> labelsOf(@NotNull String column) {
            Object value = valueOf(column);
            return switch (columnTypes.get(column)) {
                case null -> List.of();
                case "timeframe" -> getTimeframeIntervals(value).stream().map(
                        i -> ZonedDateTime.ofInstant(
                                Instant.ofEpochMilli(i.longValue()),
                                ZoneId.systemDefault()
                        ).format(DQLUtil.USER_FRIENDLY_DATE_FORMATTER)
                ).toList();
                case "array" -> {
                    List<String> result = new ArrayList<>();
                    if (value instanceof List<?> list) {
                        for (Object o : list) {
                            result.add(String.join(", ", String.valueOf(o)));
                        }
                    }
                    yield result;
                }
                default -> List.of(String.valueOf(value));
            };
        }

        private @Nullable Number convertTimestamp(@NotNull String value) {
            try {
                return Instant.from(DQLUtil.DQL_DATE_FORMATTER.parse(value)).toEpochMilli();
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }

        private @NotNull List<Number> getTimeframeIntervals(@Nullable Object value) {
            if (!(value instanceof Map<?, ?> timeframe)
                    || !(timeframe.get("start") instanceof String timeframeStart)
                    || !(timeframe.get("end") instanceof String timeframeEnd)
            ) {
                return List.of();
            }
            List<Number> result = new ArrayList<>();
            try {
                Instant current = DQLUtil.getDateFromTimestamp(timeframeStart).toInstant();
                Instant end = DQLUtil.getDateFromTimestamp(timeframeEnd).toInstant();

                if (record.get("interval") instanceof String intervalStr) {
                    long interval = Long.parseLong(intervalStr);
                    while (!current.isAfter(end) && !current.equals(end)) {
                        result.add(current.toEpochMilli());
                        current = current.plusNanos(interval);
                    }
                } else {
                    result.add(current.toEpochMilli());
                    result.add(end.toEpochMilli());
                }
            } catch (DateTimeParseException | NumberFormatException e) {
                logger.debug("Could not translate timeseries record, falling back to default representation", e);
            }
            return result;
        }
    }
}


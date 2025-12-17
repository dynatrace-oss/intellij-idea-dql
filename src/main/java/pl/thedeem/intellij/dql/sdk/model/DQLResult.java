package pl.thedeem.intellij.dql.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DQLResult {
    DQLMetadata metadata;
    List<DQLRecord> records;
    List<DQLType> types;

    public DQLMetadata getMetadata() {
        return metadata;
    }

    public DQLGrailMetadata getGrailMetadata() {
        return metadata != null && metadata.grail != null ? metadata.grail : new DQLGrailMetadata();
    }

    public List<DQLMetricMetadata> getMetricsMetadata() {
        return metadata != null && metadata.metrics != null ? metadata.metrics : List.of();
    }

    public List<DQLRecord> getRecords() {
        return records;
    }

    public List<DQLType> getTypes() {
        return types;
    }

    // The order is important, but so is the column uniqueness
    public Set<String> getColumns() {
        if (types == null) {
            return Set.of();
        }
        Set<String> result = new LinkedHashSet<>();
        for (DQLType type : types) {
            for (Map.Entry<String, DQLType.DQLFieldType> entry : type.mappings.entrySet()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Map<String, String> getColumnTypes() {
        if (types == null) {
            return Map.of();
        }
        Map<String, String> result = new HashMap<>();
        for (DQLType type : types) {
            for (Map.Entry<String, DQLType.DQLFieldType> entry : type.mappings.entrySet()) {
                if ("undefined".equals(entry.getValue().getType())) {
                    result.putIfAbsent(entry.getKey(), "undefined");
                } else {
                    result.put(entry.getKey(), entry.getValue().getType());
                }
            }
        }

        return result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DQLMetadata {
        public DQLGrailMetadata grail;
        public List<DQLMetricMetadata> metrics;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DQLGrailMetadata {
        public String canonicalQuery;
        public String timezone;
        public String query;
        public Long scannedRecords;
        public String dqlVersion;
        public Long scannedBytes;
        public Long scannedDataPoints;
        public Map<String, String> analysisTimeframe;
        public String locale;
        public Long executionTimeMilliseconds;
        public List<DQLNotification> notifications;
        public String queryId;
        public Boolean sampled;

        public String getCanonicalQuery() {
            return canonicalQuery;
        }

        public String getTimezone() {
            return timezone;
        }

        public String getQuery() {
            return query;
        }

        public Long getScannedRecords() {
            return scannedRecords;
        }

        public String getDqlVersion() {
            return dqlVersion;
        }

        public Long getScannedBytes() {
            return scannedBytes;
        }

        public Long getScannedDataPoints() {
            return scannedDataPoints;
        }

        public Map<String, String> getAnalysisTimeframe() {
            return analysisTimeframe != null ? analysisTimeframe : Map.of();
        }

        public String getAnalysisTimeframeStart() {
            return getAnalysisTimeframe().get("start");
        }

        public String getAnalysisTimeframeEnd() {
            return getAnalysisTimeframe().get("end");
        }

        public String getLocale() {
            return locale;
        }

        public Long getExecutionTimeMilliseconds() {
            return executionTimeMilliseconds;
        }

        public List<DQLNotification> getNotifications() {
            return notifications != null ? notifications : List.of();
        }

        public String getQueryId() {
            return queryId;
        }

        public Boolean isSampled() {
            return Boolean.TRUE.equals(sampled);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DQLNotification {
        public String severity;
        public String messageFormat;
        public List<Object> messageFormatSpecifierTypes;
        public List<Object> arguments;
        public String notificationType;
        public String message;
        public DQLSyntaxErrorPositionDetails syntaxPosition;

        public String getSeverity() {
            return severity;
        }

        public String getMessageFormat() {
            return messageFormat;
        }

        public List<Object> getMessageFormatSpecifierTypes() {
            return messageFormatSpecifierTypes;
        }

        public List<Object> getArguments() {
            return arguments;
        }

        public String getNotificationType() {
            return notificationType;
        }

        public String getMessage() {
            return message;
        }

        public DQLSyntaxErrorPositionDetails getSyntaxPosition() {
            return syntaxPosition;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DQLMetricMetadata extends HashMap<String, Object> {
    }
}

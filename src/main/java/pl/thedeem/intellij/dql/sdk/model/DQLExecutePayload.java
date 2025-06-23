package pl.thedeem.intellij.dql.sdk.model;

public class DQLExecutePayload {
    Long defaultScanLimitGbytes;
    String defaultTimeframeEnd;
    String defaultTimeframeStart;
    Long maxResultBytes;
    Long maxResultRecords;
    String query;

    public DQLExecutePayload(String query) {
        this.query = query;
    }

    public Long getDefaultScanLimitGbytes() {
        return defaultScanLimitGbytes;
    }

    public void setDefaultScanLimitGbytes(Long defaultScanLimitGbytes) {
        this.defaultScanLimitGbytes = defaultScanLimitGbytes;
    }

    public String getDefaultTimeframeEnd() {
        return defaultTimeframeEnd;
    }

    public void setDefaultTimeframeEnd(String defaultTimeframeEnd) {
        this.defaultTimeframeEnd = defaultTimeframeEnd;
    }

    public String getDefaultTimeframeStart() {
        return defaultTimeframeStart;
    }

    public void setDefaultTimeframeStart(String defaultTimeframeStart) {
        this.defaultTimeframeStart = defaultTimeframeStart;
    }

    public Long getMaxResultBytes() {
        return maxResultBytes;
    }

    public void setMaxResultBytes(Long maxResultBytes) {
        this.maxResultBytes = maxResultBytes;
    }

    public Long getMaxResultRecords() {
        return maxResultRecords;
    }

    public void setMaxResultRecords(Long maxResultRecords) {
        this.maxResultRecords = maxResultRecords;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}

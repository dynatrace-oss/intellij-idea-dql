package pl.thedeem.intellij.dql.services.query.model;

public class QueryConfiguration {
    private String tenant;
    private Long defaultScanLimit;
    private Long maxResultBytes;
    private Long maxResultRecords;
    private String timeframeStart;
    private String timeframeEnd;
    private String originalFile;
    private String runConfigName;

    public String tenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Long defaultScanLimit() {
        return defaultScanLimit;
    }

    public void setDefaultScanLimit(Long defaultScanLimit) {
        this.defaultScanLimit = defaultScanLimit;
    }

    public Long maxResultBytes() {
        return maxResultBytes;
    }

    public void setMaxResultBytes(Long maxResultBytes) {
        this.maxResultBytes = maxResultBytes;
    }

    public Long maxResultRecords() {
        return maxResultRecords;
    }

    public void setMaxResultRecords(Long maxResultRecords) {
        this.maxResultRecords = maxResultRecords;
    }

    public String timeframeStart() {
        return timeframeStart;
    }

    public void setTimeframeStart(String timeframeStart) {
        this.timeframeStart = timeframeStart;
    }

    public String timeframeEnd() {
        return timeframeEnd;
    }

    public void setTimeframeEnd(String timeframeEnd) {
        this.timeframeEnd = timeframeEnd;
    }

    public String originalFile() {
        return originalFile;
    }

    public void setOriginalFile(String originalFile) {
        this.originalFile = originalFile;
    }

    public String runConfigName() {
        return runConfigName;
    }

    public String getRunConfigName() {
        return runConfigName;
    }

    public void setRunConfigName(String runConfigName) {
        this.runConfigName = runConfigName;
    }

    public QueryConfiguration copy() {
        QueryConfiguration copy = new QueryConfiguration();
        copy.setTenant(this.tenant);
        copy.setDefaultScanLimit(this.defaultScanLimit);
        copy.setMaxResultBytes(this.maxResultBytes);
        copy.setMaxResultRecords(this.maxResultRecords);
        copy.setTimeframeStart(this.timeframeStart);
        copy.setTimeframeEnd(this.timeframeEnd);
        copy.setOriginalFile(this.originalFile);
        copy.setRunConfigName(this.runConfigName);
        return copy;
    }
}

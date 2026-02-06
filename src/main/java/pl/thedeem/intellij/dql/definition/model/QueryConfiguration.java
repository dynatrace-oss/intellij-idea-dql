package pl.thedeem.intellij.dql.definition.model;

import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;

import java.util.List;

public class QueryConfiguration {
    private String tenant;
    private String query;
    private Long defaultScanLimit;
    private Long maxResultBytes;
    private Long maxResultRecords;
    private String timeframeStart;
    private String timeframeEnd;
    private String runConfigName;
    private String originalFile;
    private List<DQLVariablesService.VariableDefinition> definedVariables;

    public String tenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String query() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public String getRunConfigName() {
        return runConfigName;
    }

    public void setRelatedRunConfiguration(String name) {
        this.runConfigName = name;
    }

    public String runConfigName() {
        return runConfigName;
    }

    public void setRunConfigName(String runConfigName) {
        this.runConfigName = runConfigName;
    }

    public String originalFile() {
        return originalFile;
    }

    public void setOriginalFile(String originalFile) {
        this.originalFile = originalFile;
    }

    public List<DQLVariablesService.VariableDefinition> definedVariables() {
        return definedVariables;
    }

    public void setDefinedVariables(List<DQLVariablesService.VariableDefinition> definedVariables) {
        this.definedVariables = definedVariables;
    }

    public QueryConfiguration copy() {
        QueryConfiguration copy = new QueryConfiguration();
        copy.setTenant(this.tenant);
        copy.setQuery(this.query);
        copy.setDefaultScanLimit(this.defaultScanLimit);
        copy.setMaxResultBytes(this.maxResultBytes);
        copy.setMaxResultRecords(this.maxResultRecords);
        copy.setTimeframeStart(this.timeframeStart);
        copy.setTimeframeEnd(this.timeframeEnd);
        copy.setRunConfigName(this.runConfigName);
        copy.setOriginalFile(this.originalFile);
        copy.setDefinedVariables(this.definedVariables != null ? List.copyOf(this.definedVariables) : null);
        return copy;
    }
}

package pl.thedeem.intellij.dql.executing.runConfiguration;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class ExecuteDQLRunConfigurationOptions extends RunConfigurationOptions {
    private final StoredProperty<Boolean> fromFileSelected = property(true).provideDelegate(this, "fromFileSelected");
    private final StoredProperty<String> dqlPath = string("").provideDelegate(this, "dqlPath");
    private final StoredProperty<String> dqlQuery = string("").provideDelegate(this, "dqlQuery");
    private final StoredProperty<String> selectedTenant = string("").provideDelegate(this, "selectedTenant");
    private final StoredProperty<String> defaultScanLimit = string("").provideDelegate(this, "defaultScanLimit");
    private final StoredProperty<String> maxResultBytes = string("").provideDelegate(this, "maxResultBytes");
    private final StoredProperty<String> maxResultRecords = string("").provideDelegate(this, "maxResultRecords");
    private final StoredProperty<String> timeframeStart = string("").provideDelegate(this, "timeframeStart");
    private final StoredProperty<String> timeframeEnd = string("").provideDelegate(this, "timeframeEnd");

    public String getDqlPath() {
        return dqlPath.getValue(this);
    }

    public String getDqlQuery() {
        return dqlQuery.getValue(this);
    }

    public void setDqlQuery(String dqlQuery) {
        this.dqlQuery.setValue(this, dqlQuery);
    }

    // could be simplified in the future. It's comparing to FALSE ensuring old run configurations without the property still work
    public Boolean getFromFileSelected() {
        return !Boolean.FALSE.equals(fromFileSelected.getValue(this));
    }

    public void setFromFileSelected(Boolean fromFileSelected) {
        this.fromFileSelected.setValue(this, fromFileSelected);
    }

    public String getSelectedTenant() {
        return selectedTenant.getValue(this);
    }

    public void setSelectedTenant(String tenant) {
        this.selectedTenant.setValue(this, tenant);
    }

    public void setDqlPath(String dqlPath) {
        this.dqlPath.setValue(this, dqlPath);
    }

    public Long getDefaultScanLimit() {
        return getLongValue(defaultScanLimit);
    }

    public void setDefaultScanLimit(Long limit) {
        this.defaultScanLimit.setValue(this, limit != null ? limit.toString() : null);
    }

    public Long getMaxResultBytes() {
        return getLongValue(maxResultBytes);
    }

    public void setMaxResultBytes(Long size) {
        this.maxResultBytes.setValue(this, size != null ? size.toString() : null);
    }

    public Long getMaxResultRecords() {
        return getLongValue(maxResultRecords);
    }

    public void setMaxResultRecords(Long size) {
        this.maxResultRecords.setValue(this, size != null ? size.toString() : null);
    }

    public String getTimeframeStart() {
        return timeframeStart.getValue(this);
    }

    public void setTimeframeStart(String start) {
        this.timeframeStart.setValue(this, start);
    }

    public String getTimeframeEnd() {
        return timeframeEnd.getValue(this);
    }

    public void setTimeframeEnd(String end) {
        this.timeframeEnd.setValue(this, end);
    }

    private Long getLongValue(StoredProperty<String> property) {
        try {
            return Long.parseLong(property.getValue(this));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

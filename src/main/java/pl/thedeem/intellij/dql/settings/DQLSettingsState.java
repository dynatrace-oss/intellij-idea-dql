package pl.thedeem.intellij.dql.settings;

import com.intellij.util.xmlb.annotations.OptionTag;

import java.util.Objects;

public class DQLSettingsState {
    @OptionTag
    public boolean calculateFieldsDataType;
    @OptionTag
    public boolean performLiveValidation;
    @OptionTag
    public boolean useDynatraceAutocomplete;
    @OptionTag
    public boolean allowExperimentalFeatures;
    @OptionTag
    public String defaultTenantUrl;
    @OptionTag
    public boolean showDqlExecutionToolbar = true;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DQLSettingsState that = (DQLSettingsState) o;
        return calculateFieldsDataType == that.calculateFieldsDataType
                && performLiveValidation == that.performLiveValidation
                && useDynatraceAutocomplete == that.useDynatraceAutocomplete
                && allowExperimentalFeatures == that.allowExperimentalFeatures
                && showDqlExecutionToolbar == that.showDqlExecutionToolbar
                && Objects.equals(defaultTenantUrl, that.defaultTenantUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                calculateFieldsDataType,
                performLiveValidation,
                useDynatraceAutocomplete,
                allowExperimentalFeatures,
                showDqlExecutionToolbar,
                defaultTenantUrl
        );
    }
}

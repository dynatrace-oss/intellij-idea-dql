package pl.thedeem.intellij.dql.settings;

import com.intellij.util.xmlb.annotations.OptionTag;

import java.util.Objects;

public class DQLSettingsState {
    @OptionTag
    public boolean calculateFieldsDataType;
    @OptionTag
    public boolean calculateExpressionsDataType;
    @OptionTag
    public boolean performLiveValidation;
    @OptionTag
    public String defaultTenantUrl;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DQLSettingsState that = (DQLSettingsState) o;
        return calculateFieldsDataType == that.calculateFieldsDataType
                && calculateExpressionsDataType == that.calculateExpressionsDataType
                && performLiveValidation == that.performLiveValidation
                && Objects.equals(defaultTenantUrl, that.defaultTenantUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                calculateFieldsDataType,
                calculateExpressionsDataType,
                performLiveValidation,
                defaultTenantUrl
        );
    }
}

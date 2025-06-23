package pl.thedeem.intellij.dql.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "DQLSettings", storages = @Storage("dql-settings.xml"))
public class DQLSettings implements PersistentStateComponent<DQLSettingsState> {
    private DQLSettingsState myState = new DQLSettingsState();

    public static DQLSettings getInstance() {
        return ApplicationManager.getApplication().getService(DQLSettings.class);
    }

    @Override
    public @Nullable DQLSettingsState getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull DQLSettingsState dqlSettingsState) {
        myState = dqlSettingsState;
    }

    public boolean isCalculatingFieldsDataTypesEnabled() {
        return myState.calculateFieldsDataType;
    }

    public void setCalculatingFieldsDataTypesEnabled(boolean enabled) {
        myState.calculateFieldsDataType = enabled;
    }

    public boolean isCalculatingExpressionDataTypesEnabled() {
        return myState.calculateExpressionsDataType;
    }

    public void setCalculatingExpressionDataTypesEnabled(boolean enabled) {
        myState.calculateExpressionsDataType = enabled;
    }

    public boolean isPerformingLiveValidationEnabled() {
        return myState.performLiveValidation;
    }

    public void setPerformingLiveValidationEnabled(boolean enabled) {
        myState.performLiveValidation = enabled;
    }

    public String getDefaultLiveValidationsTenant() {
        return myState.defaultTenantUrl;
    }

    public void setDefaultLiveValidationsTenant(String tenant) {
        myState.defaultTenantUrl = tenant;
    }
}

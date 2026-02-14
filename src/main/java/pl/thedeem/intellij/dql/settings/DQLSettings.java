package pl.thedeem.intellij.dql.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.util.List;

@State(name = "DQLSettings", storages = @Storage("dql-settings.xml"))
public class DQLSettings implements PersistentStateComponent<DQLSettingsState> {
    private DQLSettingsState myState = new DQLSettingsState();
    public static Key<Boolean> EXTERNAL_VALIDATION_ENABLED = new Key<>("DQL_EXTERNAL_VALIDATION_ENABLED");

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

    public boolean isAllowingExperimentalFeatures() {
        return myState.allowExperimentalFeatures;
    }

    public void setAllowingExperimentalFeatures(boolean enabled) {
        myState.allowExperimentalFeatures = enabled;
    }

    public boolean isPerformingLiveValidationEnabled() {
        return myState.performLiveValidation;
    }

    public void setPerformingLiveValidationEnabled(boolean enabled) {
        myState.performLiveValidation = enabled;
    }

    public boolean isUseDynatraceAutocompleteEnabled() {
        return myState.useDynatraceAutocomplete;
    }

    public void setUseDynatraceAutocompleteEnabled(boolean enabled) {
        myState.useDynatraceAutocomplete = enabled;
    }

    public String getDefaultDynatraceTenant() {
        List<DynatraceTenant> tenants = DynatraceTenantsService.getInstance().getTenants();
        return !tenants.isEmpty() ? tenants.getFirst().getName() : null;
    }

    public boolean isDQLExecutionToolbarVisible() {
        return myState.showDqlExecutionToolbar;
    }

    public void setDQLExecutionToolbarVisible(boolean enabled) {
        myState.showDqlExecutionToolbar = enabled;
    }
}

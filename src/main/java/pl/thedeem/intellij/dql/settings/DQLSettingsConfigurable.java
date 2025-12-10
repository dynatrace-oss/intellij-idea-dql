package pl.thedeem.intellij.dql.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.util.Objects;

public class DQLSettingsConfigurable implements Configurable {
    private DQLSettingsComponent mySettingsComponent;

    private final DQLSettings mySettings = DQLSettings.getInstance();

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return DQLBundle.message("settings.dql.title");
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mySettingsComponent = new DQLSettingsComponent(mySettings);
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        return mySettingsComponent.isCalculatingFieldsDataTypesEnabled() != mySettings.isCalculatingFieldsDataTypesEnabled()
                || mySettingsComponent.isAllowingExperimentalFeatures() != mySettings.isAllowingExperimentalFeatures()
                || mySettingsComponent.isPerformingLiveValidationEnabled() != mySettings.isPerformingLiveValidationEnabled()
                || mySettingsComponent.isUseDynatraceAutocompleteEnabled() != mySettings.isUseDynatraceAutocompleteEnabled()
                || !Objects.equals(mySettingsComponent.getDefaultDynatraceTenant(), mySettings.getDefaultDynatraceTenant())
                ;
    }

    @Override
    public void apply() {
        mySettings.setCalculatingFieldsDataTypesEnabled(mySettingsComponent.isCalculatingFieldsDataTypesEnabled());
        mySettings.setAllowingExperimentalFeatures(mySettingsComponent.isAllowingExperimentalFeatures());
        mySettings.setPerformingLiveValidationEnabled(mySettingsComponent.isPerformingLiveValidationEnabled());
        mySettings.setUseDynatraceAutocompleteEnabled(mySettingsComponent.isUseDynatraceAutocompleteEnabled());
        mySettings.setDefaultDynatraceTenant(mySettingsComponent.getDefaultDynatraceTenant());
    }

    @Override
    public void reset() {
        mySettingsComponent.setCalculatingFieldsDataTypesEnabled(mySettings.isCalculatingFieldsDataTypesEnabled());
        mySettingsComponent.setAllowingExperimentalFeatures(mySettings.isAllowingExperimentalFeatures());
        mySettingsComponent.setPerformingLiveValidationEnabled(mySettings.isPerformingLiveValidationEnabled());
        mySettingsComponent.setUseDynatraceAutocompleteEnabled(mySettings.isUseDynatraceAutocompleteEnabled());
        mySettingsComponent.setDefaultDynatraceTenant(mySettings.getDefaultDynatraceTenant());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}

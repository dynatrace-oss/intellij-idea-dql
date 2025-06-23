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
                || mySettingsComponent.isCalculatingExpressionDataTypesEnabled() != mySettings.isCalculatingExpressionDataTypesEnabled()
                || mySettingsComponent.isPerformingLiveValidationEnabled() != mySettings.isPerformingLiveValidationEnabled()
                || !Objects.equals(mySettingsComponent.getDefaultLiveValidationsTenant(), mySettings.getDefaultLiveValidationsTenant())
                ;
    }

    @Override
    public void apply() {
        mySettings.setCalculatingFieldsDataTypesEnabled(mySettingsComponent.isCalculatingFieldsDataTypesEnabled());
        mySettings.setCalculatingExpressionDataTypesEnabled(mySettingsComponent.isCalculatingExpressionDataTypesEnabled());
        mySettings.setPerformingLiveValidationEnabled(mySettingsComponent.isPerformingLiveValidationEnabled());
        mySettings.setDefaultLiveValidationsTenant(mySettingsComponent.getDefaultLiveValidationsTenant());
    }

    @Override
    public void reset() {
        mySettingsComponent.setCalculatingFieldsDataTypesEnabled(mySettings.isCalculatingFieldsDataTypesEnabled());
        mySettingsComponent.setCalculatingExpressionDataTypesEnabled(mySettings.isCalculatingExpressionDataTypesEnabled());
        mySettingsComponent.setPerformingLiveValidationEnabled(mySettings.isPerformingLiveValidationEnabled());
        mySettingsComponent.setDefaultLiveValidationsTenant(mySettings.getDefaultLiveValidationsTenant());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}

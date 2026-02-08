package pl.thedeem.intellij.dql.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;

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
                || mySettingsComponent.isDQLExecutionToolbarVisible() != mySettings.isDQLExecutionToolbarVisible();
    }

    @Override
    public void apply() {
        mySettings.setAllowingExperimentalFeatures(mySettingsComponent.isAllowingExperimentalFeatures());
        mySettings.setCalculatingFieldsDataTypesEnabled(mySettingsComponent.isCalculatingFieldsDataTypesEnabled());
        mySettings.setPerformingLiveValidationEnabled(mySettingsComponent.isPerformingLiveValidationEnabled());
        mySettings.setUseDynatraceAutocompleteEnabled(mySettingsComponent.isUseDynatraceAutocompleteEnabled());
        mySettings.setDQLExecutionToolbarVisible(mySettingsComponent.isDQLExecutionToolbarVisible());
    }

    @Override
    public void reset() {
        mySettingsComponent.setCalculatingFieldsDataTypesEnabled(mySettings.isCalculatingFieldsDataTypesEnabled());
        mySettingsComponent.setAllowingExperimentalFeatures(mySettings.isAllowingExperimentalFeatures());
        mySettingsComponent.setPerformingLiveValidationEnabled(mySettings.isPerformingLiveValidationEnabled());
        mySettingsComponent.setUseDynatraceAutocompleteEnabled(mySettings.isUseDynatraceAutocompleteEnabled());
        mySettingsComponent.setDQLExecutionToolbarVisible(mySettings.isDQLExecutionToolbarVisible());
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }

    public static void showSettings(@NotNull Project project) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, DQLSettingsConfigurable.class);
    }
}

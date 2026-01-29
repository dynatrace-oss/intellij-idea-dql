package pl.thedeem.intellij.dql.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.FormBuilder;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;

public class DQLSettingsComponent {
    private final JPanel myMainPanel;
    private final JBCheckBox calculateFieldsDataType = new JBCheckBox(DQLBundle.message("settings.dql.features.calculateFieldsDataType"));
    private final JBCheckBox allowExperimentalFeatures = new JBCheckBox(DQLBundle.message("settings.dql.features.allowExperimentalFeatures"));
    private final JBCheckBox performLiveValidations = new JBCheckBox(DQLBundle.message("settings.dql.features.performLiveValidations"));
    private final JBCheckBox useDynatraceAutocomplete = new JBCheckBox(DQLBundle.message("settings.dql.features.useDynatraceAutocomplete"));
    private final JBCheckBox showDqlExecutionToolbar = new JBCheckBox(DQLBundle.message("settings.dql.features.showDqlExecutionToolbar"));

    public DQLSettingsComponent(DQLSettings settings) {
        calculateFieldsDataType.setToolTipText(DQLBundle.message("settings.dql.features.calculateFieldsDataTypeDescription"));
        allowExperimentalFeatures.setToolTipText(DQLBundle.message("settings.dql.features.allowExperimentalFeaturesDescription"));

        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(showDqlExecutionToolbar, 1)
                .addComponent(calculateFieldsDataType, 1)
                .addComponent(allowExperimentalFeatures, 1)
                .addComponent(performLiveValidations, 1)
                .addComponent(useDynatraceAutocomplete, 1)
                .addComponentFillVertically(new JBPanel<>(), 0)
                .getPanel();

        // Initialize checkbox state from current settings
        calculateFieldsDataType.setSelected(settings.isCalculatingFieldsDataTypesEnabled());
        allowExperimentalFeatures.setSelected(settings.isAllowingExperimentalFeatures());
        performLiveValidations.setSelected(settings.isPerformingLiveValidationEnabled());
        useDynatraceAutocomplete.setSelected(settings.isUseDynatraceAutocompleteEnabled());
        showDqlExecutionToolbar.setSelected(settings.isDQLExecutionToolbarVisible());

    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public boolean isCalculatingFieldsDataTypesEnabled() {
        return calculateFieldsDataType.isSelected();
    }

    public void setCalculatingFieldsDataTypesEnabled(boolean enabled) {
        calculateFieldsDataType.setSelected(enabled);
    }

    public boolean isAllowingExperimentalFeatures() {
        return allowExperimentalFeatures.isSelected();
    }

    public void setAllowingExperimentalFeatures(boolean enabled) {
        allowExperimentalFeatures.setSelected(enabled);
    }

    public boolean isPerformingLiveValidationEnabled() {
        return performLiveValidations.isSelected();
    }

    public void setPerformingLiveValidationEnabled(boolean enabled) {
        performLiveValidations.setSelected(enabled);
    }

    public boolean isUseDynatraceAutocompleteEnabled() {
        return useDynatraceAutocomplete.isSelected();
    }

    public void setUseDynatraceAutocompleteEnabled(boolean enabled) {
        useDynatraceAutocomplete.setSelected(enabled);
    }

    public boolean isDQLExecutionToolbarVisible() {
        return showDqlExecutionToolbar.isSelected();
    }

    public void setDQLExecutionToolbarVisible(boolean visible) {
        showDqlExecutionToolbar.setSelected(visible);
    }
}

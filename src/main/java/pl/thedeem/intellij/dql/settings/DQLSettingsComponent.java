package pl.thedeem.intellij.dql.settings;

import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.FormBuilder;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.components.DynatraceTenantSelector;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;

import javax.swing.*;
import java.awt.*;

public class DQLSettingsComponent {
    private final JPanel myMainPanel;
    private final JBCheckBox calculateFieldsDataType = new JBCheckBox(DQLBundle.message("settings.dql.features.calculateFieldsDataType"));
    private final JBCheckBox allowExperimentalFeatures = new JBCheckBox(DQLBundle.message("settings.dql.features.allowExperimentalFeatures"));
    private final JBCheckBox performLiveValidations = new JBCheckBox(DQLBundle.message("settings.dql.features.performLiveValidations"));
    private final JBCheckBox useDynatraceAutocomplete = new JBCheckBox(DQLBundle.message("settings.dql.features.useDynatraceAutocomplete"));
    private final JBCheckBox showDqlExecutionToolbar = new JBCheckBox(DQLBundle.message("settings.dql.features.showDqlExecutionToolbar"));
    private final JBCheckBox showDqlInjectionGutterIcon = new JBCheckBox(DQLBundle.message("settings.dql.features.showDqlInjectionGutterIcon"));
    private final DynatraceTenantSelector<?> defaultDynatraceTenant = new DynatraceTenantSelector<>(new FlowLayout(FlowLayout.LEFT, 0, 0));

    public DQLSettingsComponent(DQLSettings settings) {
        calculateFieldsDataType.setToolTipText(DQLBundle.message("settings.dql.features.calculateFieldsDataTypeDescription"));
        allowExperimentalFeatures.setToolTipText(DQLBundle.message("settings.dql.features.allowExperimentalFeaturesDescription"));
        JPanel tenantsSelectorPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(DQLBundle.message("settings.dql.features.defaultDynatraceTenant"), defaultDynatraceTenant)
                .addTooltip(DQLBundle.message("settings.dql.features.defaultDynatraceTenantDescription"))
                .getPanel();

        myMainPanel = FormBuilder.createFormBuilder()
                .addComponent(showDqlExecutionToolbar, 1)
                .addComponent(showDqlInjectionGutterIcon, 1)
                .addComponent(calculateFieldsDataType, 1)
                .addComponent(allowExperimentalFeatures, 1)
                .addComponent(performLiveValidations, 1)
                .addComponent(useDynatraceAutocomplete, 1)
                .addComponent(tenantsSelectorPanel, 1)
                .addComponentFillVertically(new JBPanel<>(), 0)
                .getPanel();

        // Initialize checkbox state from current settings
        calculateFieldsDataType.setSelected(settings.isCalculatingFieldsDataTypesEnabled());
        allowExperimentalFeatures.setSelected(settings.isAllowingExperimentalFeatures());
        performLiveValidations.setSelected(settings.isPerformingLiveValidationEnabled());
        useDynatraceAutocomplete.setSelected(settings.isUseDynatraceAutocompleteEnabled());
        showDqlExecutionToolbar.setSelected(settings.isDQLExecutionToolbarVisible());
        showDqlInjectionGutterIcon.setSelected(settings.isDQLInjectionGutterIconVisible());
        performLiveValidations.addActionListener((actionEvent -> tenantsSelectorPanel.setVisible(performLiveValidations.isSelected() || useDynatraceAutocomplete.isSelected())));
        useDynatraceAutocomplete.addActionListener((actionEvent -> tenantsSelectorPanel.setVisible(performLiveValidations.isSelected() || useDynatraceAutocomplete.isSelected())));
        tenantsSelectorPanel.setVisible(settings.isPerformingLiveValidationEnabled());
        defaultDynatraceTenant.refreshTenantsComboBox();
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

    public void setDefaultDynatraceTenant(String tenant) {
        defaultDynatraceTenant.selectTenant(tenant);
    }

    public String getDefaultDynatraceTenant() {
        DynatraceTenant selectedTenant = defaultDynatraceTenant.getSelectedTenant();
        return selectedTenant != null ? selectedTenant.getName() : null;
    }

    public boolean isDQLExecutionToolbarVisible() {
        return showDqlExecutionToolbar.isSelected();
    }

    public void setDQLExecutionToolbarVisible(boolean visible) {
        showDqlExecutionToolbar.setSelected(visible);
    }

    public boolean isDQLInjectionGutterIconVisible() {
        return showDqlInjectionGutterIcon.isSelected();
    }

    public void setDQLInjectionGutterIconVisible(boolean visible) {
        showDqlInjectionGutterIcon.setSelected(visible);
    }

}

package pl.thedeem.intellij.dql.components;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBPanel;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsConfigurable;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DynatraceTenantSelector<T extends JBPanel<?>> extends JBPanel<T> {
    private final DynatraceTenantsService tenantsService;
    private final DefaultComboBoxModel<DynatraceTenant> comboBoxModel;
    private final ComboBox<DynatraceTenant> tenantComboBox;

    public DynatraceTenantSelector(LayoutManager layout) {
        super(layout);
        comboBoxModel = new DefaultComboBoxModel<>();
        tenantsService = DynatraceTenantsService.getInstance();
        tenantComboBox = new ComboBox<>(comboBoxModel);

        JButton addTenantButton = new JButton(DQLBundle.message("components.tenantsSelector.manageTenants"));
        addTenantButton.addActionListener(e -> {
            boolean edited = DynatraceTenantsConfigurable.showSettings();
            if (edited) {
                refreshTenantsComboBox();
            }
        });

        add(tenantComboBox);
        add(addTenantButton);
    }

    public void selectTenant(@Nullable String tenantName) {
        if (tenantName != null && !tenantName.isEmpty()) {
            for (int i = 0; i < comboBoxModel.getSize(); i++) {
                DynatraceTenant tenant = comboBoxModel.getElementAt(i);
                if (tenant != null && tenantName.equals(tenant.getName())) {
                    tenantComboBox.setSelectedItem(tenant);
                    return;
                }
            }
            tenantComboBox.setSelectedItem(null);
        } else {
            tenantComboBox.setSelectedItem(null);
        }
    }

    public DynatraceTenant getSelectedTenant() {
        return (DynatraceTenant) tenantComboBox.getSelectedItem();
    }

    public void refreshTenantsComboBox() {
        List<DynatraceTenant> tenants = tenantsService.getTenants();
        DynatraceTenant currentlySelected = getSelectedTenant();
        comboBoxModel.removeAllElements();
        for (DynatraceTenant tenant : tenants) {
            comboBoxModel.addElement(tenant);
        }
        if (currentlySelected != null) {
            selectTenant(currentlySelected.getName());
        }
        else {
            tenantComboBox.setSelectedItem(null);
        }
    }
}

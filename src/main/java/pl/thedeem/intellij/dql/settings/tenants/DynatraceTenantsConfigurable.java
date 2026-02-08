package pl.thedeem.intellij.dql.settings.tenants;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class DynatraceTenantsConfigurable implements Configurable {

    private final DynatraceTenantsService mySettings = DynatraceTenantsService.getInstance();
    private JBList<DynatraceTenant> tenantList;
    private DefaultListModel<DynatraceTenant> listModel;

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return DQLBundle.message("settings.dql.tenants.title");
    }

    @Override
    public @Nullable JComponent createComponent() {
        listModel = new DefaultListModel<>();
        mySettings.getTenants().forEach(listModel::addElement);

        tenantList = new JBList<>(listModel);
        tenantList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(tenantList)
                .setAddAction(button -> addTenant())
                .setEditAction(button -> editTenant())
                .setRemoveAction(button -> removeTenant());

        BorderLayoutPanel panel = JBUI.Panels.simplePanel();
        panel.addToCenter(decorator.createPanel());
        return panel;
    }

    private void addTenant() {
        TenantSettingsDialog dialog = new TenantSettingsDialog(null);
        if (dialog.showAndGet()) {
            DynatraceTenant newTenant = dialog.getTenant();
            if (newTenant != null) {
                listModel.addElement(newTenant);
            }
        }
    }

    private void editTenant() {
        DynatraceTenant selectedTenant = tenantList.getSelectedValue();
        if (selectedTenant != null) {
            TenantSettingsDialog dialog = new TenantSettingsDialog(selectedTenant);
            if (dialog.showAndGet()) {
                DynatraceTenant updatedTenant = dialog.getTenant();
                if (updatedTenant != null) {
                    int index = listModel.indexOf(selectedTenant);
                    if (index != -1) {
                        listModel.set(index, updatedTenant);
                    }
                }
            }
        }
    }

    private void removeTenant() {
        int selectedIndex = tenantList.getSelectedIndex();
        mySettings.removeTenant(tenantList.getSelectedValue());
        if (selectedIndex != -1) {
            listModel.remove(selectedIndex);
        }
    }

    @Override
    public boolean isModified() {
        List<DynatraceTenant> currentTenants = mySettings.getTenants();
        List<DynatraceTenant> uiTenants = new ArrayList<>();
        for (int i = 0; i < listModel.getSize(); i++) {
            uiTenants.add(listModel.getElementAt(i));
        }
        return !currentTenants.equals(uiTenants);
    }

    @Override
    public void apply() {
        mySettings.getTenants().clear();
        for (int i = 0; i < listModel.getSize(); i++) {
            mySettings.addTenant(listModel.getElementAt(i));
        }
    }

    @Override
    public void reset() {
        listModel.clear();
        mySettings.getTenants().forEach(listModel::addElement);
    }

    @Override
    public void disposeUIResources() {
        listModel = null;
        tenantList = null;
    }

    public static boolean showSettings() {
        return ShowSettingsUtil.getInstance().editConfigurable(
                ProjectManager.getInstance().getDefaultProject(),
                new DynatraceTenantsConfigurable()
        );
    }

    public static void showSettings(@NotNull String tenant) {
        DynatraceTenantsConfigurable settings = new DynatraceTenantsConfigurable();
        ShowSettingsUtil.getInstance().editConfigurable(
                ProjectManager.getInstance().getDefaultProject(),
                settings,
                () -> {
                    for (int i = 0; i < settings.listModel.getSize(); i++) {
                        if (settings.listModel.getElementAt(i).getName().equals(tenant)) {
                            settings.tenantList.setSelectedIndex(i);
                            settings.editTenant();
                            break;
                        }
                    }
                }
        );
    }
}

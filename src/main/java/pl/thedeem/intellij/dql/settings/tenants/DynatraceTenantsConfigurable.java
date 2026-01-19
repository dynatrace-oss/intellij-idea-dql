package pl.thedeem.intellij.dql.settings.tenants;

import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

        BorderLayoutPanel panel = new BorderLayoutPanel();
        panel.addToCenter(decorator.createPanel());
        return panel;
    }

    private void addTenant() {
        TenantDialog dialog = new TenantDialog(null);
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
            TenantDialog dialog = new TenantDialog(selectedTenant);
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

    private static class TenantDialog extends DialogWrapper {
        private JBTextField name;
        private JBTextField urlField;
        private JPasswordField apiTokenField;
        private DynatraceTenant tenant;

        protected TenantDialog(@Nullable DynatraceTenant existingTenant) {
            super(true);
            this.tenant = existingTenant;
            setTitle(DQLBundle.message(existingTenant == null ? "settings.dql.tenants.add" : "settings.dql.tenants.edit"));
            init();
        }

        public DynatraceTenant getTenant() {
            return tenant;
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            name = new JBTextField(30);
            urlField = new JBTextField(100);
            apiTokenField = new JPasswordField(40);

            if (tenant != null) {
                name.setText(tenant.getName());
                urlField.setText(tenant.getUrl());
                String storedToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
                apiTokenField.setText(storedToken);
            }

            return FormBuilder.createFormBuilder()
                    .addLabeledComponent(DQLBundle.message("settings.dql.tenants.form.name"), name)
                    .addLabeledComponent(DQLBundle.message("settings.dql.tenants.form.url"), urlField)
                    .addLabeledComponent(DQLBundle.message("settings.dql.tenants.form.token"), apiTokenField)
                    .getPanel();
        }

        @Override
        public void doCancelAction() {
            char[] apiTokenChars = apiTokenField.getPassword();
            if (apiTokenChars != null) {
                java.util.Arrays.fill(apiTokenChars, ' ');
            }
            super.doCancelAction();
        }

        @Override
        protected void doOKAction() {
            String apiToken = apiTokenField != null ? new String(apiTokenField.getPassword()) : "";
            if (urlField.getText().isEmpty() || apiToken.isEmpty()) {
                setErrorText(DQLBundle.message("settings.dql.tenants.form.error.missingFields"));
                return;
            }

            try {
                URL ignored = URI.create(urlField.getText()).toURL();
            } catch (MalformedURLException | IllegalArgumentException e) {
                setErrorText(DQLBundle.message("settings.dql.tenants.form.error.invalidTenantUrl", e.getMessage()));
                return;
            }

            if (name.getText().isEmpty()) {
                name.setText(urlField.getText());
            }

            String credentialId;
            if (tenant != null && tenant.getCredentialId() != null) {
                credentialId = tenant.getCredentialId();
            } else {
                credentialId = UUID.randomUUID().toString();
            }
            Credentials credentials = new Credentials(name.getText(), apiToken);
            PasswordSafe.getInstance().set(DQLUtil.createCredentialAttributes(credentialId), credentials);
            tenant = new DynatraceTenant(name.getText(), urlField.getText(), credentialId);
            super.doOKAction();
        }
    }
}
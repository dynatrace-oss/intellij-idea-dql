package pl.thedeem.intellij.dql.settings.tenants;

import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.LoadingPanel;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

public class TenantSettingsDialog extends DialogWrapper {
    private JBTextField name;
    private JBTextField urlField;
    private JBPasswordField apiTokenField;
    private DynatraceTenant tenant;

    protected TenantSettingsDialog(@Nullable DynatraceTenant existingTenant) {
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
        apiTokenField = new JBPasswordField();
        BorderLayoutPanel passwordContainer = new BorderLayoutPanel();

        if (tenant != null) {
            name.setText(tenant.getName());
            urlField.setText(tenant.getUrl());

            LoadingPanel loading = new LoadingPanel("Loading");
            passwordContainer.addToLeft(loading);

            String credentialId = tenant.getCredentialId();
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                String storedToken = StringUtil.isNotEmpty(credentialId) ? PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(credentialId)) : null;
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (isDisposed()) {
                        return;
                    }
                    passwordContainer.removeAll();
                    apiTokenField.setText(storedToken);
                    passwordContainer.addToLeft(apiTokenField);
                    passwordContainer.revalidate();
                    passwordContainer.repaint();
                    loading.dispose();
                }, ModalityState.stateForComponent(passwordContainer));
            });
        } else {
            passwordContainer.addToLeft(apiTokenField);
        }

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(DQLBundle.message("settings.dql.tenants.form.name"), name)
                .addLabeledComponent(DQLBundle.message("settings.dql.tenants.form.url"), urlField)
                .addLabeledComponent(DQLBundle.message("settings.dql.tenants.form.token"), passwordContainer)
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

        String credentialId = tenant != null && StringUtil.isNotEmpty(tenant.getCredentialId()) ?
                tenant.getCredentialId() : UUID.randomUUID().toString();
        Credentials credentials = new Credentials(name.getText(), apiToken);
        PasswordSafe.getInstance().set(DQLUtil.createCredentialAttributes(credentialId), credentials);
        tenant = new DynatraceTenant(name.getText(), urlField.getText(), credentialId);
        super.doOKAction();
    }
}

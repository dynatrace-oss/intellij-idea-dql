package pl.thedeem.intellij.dql.settings.tenants;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.simple.SearchableComboBox;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TenantSettingsDialog extends DialogWrapper {
    private JBTextField name;
    private JBTextField urlField;
    private DynatraceTenant tenant;

    private SearchableComboBox<DynatraceTenant.AuthType> authenticationMethod;
    private ApiTokenPanel apiTokenPanel;
    private OAuthSignInPanel oauthPanel;
    private JBPanel<?> authCardPanel;

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
        name = new JBTextField();
        urlField = new JBTextField();

        apiTokenPanel = new ApiTokenPanel();
        oauthPanel = new OAuthSignInPanel(urlField::getText);

        CardLayout authLayout = new CardLayout();
        authCardPanel = new JBPanel<>(authLayout);
        authCardPanel.add(apiTokenPanel, DynatraceTenant.AuthType.API_TOKEN.name());
        authCardPanel.add(oauthPanel, DynatraceTenant.AuthType.SSO_OAUTH.name());

        authenticationMethod = new SearchableComboBox<>(List.of(DynatraceTenant.AuthType.values()), DynatraceTenant.AuthType.SSO_OAUTH, null, authType -> {
            if (authType == DynatraceTenant.AuthType.SSO_OAUTH) {
                authLayout.show(authCardPanel, DynatraceTenant.AuthType.SSO_OAUTH.name());
                oauthPanel.init(tenant);
            } else {
                authLayout.show(authCardPanel, DynatraceTenant.AuthType.API_TOKEN.name());
                apiTokenPanel.init(tenant);
            }
        }) {{
            setRenderer(SimpleListCellRenderer.create("", position -> switch (position) {
                case DynatraceTenant.AuthType.SSO_OAUTH ->
                        DQLBundle.message("settings.dql.tenants.form.authType.ssoOauth");
                case null, default -> DQLBundle.message("settings.dql.tenants.form.authType.apiToken");
            }));
        }};

        if (tenant != null) {
            name.setText(tenant.getName());
            urlField.setText(tenant.getUrl());
            authenticationMethod.setSelectedItem(tenant.getAuthType());
            authLayout.show(authCardPanel, tenant.getAuthType().name());
            if (tenant.getAuthType() == DynatraceTenant.AuthType.SSO_OAUTH) {
                oauthPanel.init(tenant);
            } else {
                apiTokenPanel.init(tenant);
            }
        } else {
            authLayout.show(authCardPanel, DynatraceTenant.AuthType.SSO_OAUTH.name());
            oauthPanel.init(null);
        }

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(DQLBundle.message("settings.dql.tenants.form.name"), name)
                .addLabeledComponent(DQLBundle.message("settings.dql.tenants.form.url"), urlField)
                .addLabeledComponent(DQLBundle.message("settings.dql.tenants.form.authType"), authenticationMethod)
                .addComponentToRightColumn(authCardPanel)
                .getPanel();
    }

    @Override
    public void doCancelAction() {
        if (oauthPanel != null) {
            oauthPanel.dispose();
        }
        if (apiTokenPanel != null) {
            apiTokenPanel.dispose();
        }
        super.doCancelAction();
    }

    @Override
    protected void doOKAction() {
        if (name.getText().isEmpty()) {
            name.setText(urlField.getText());
        }

        String urlText = urlField.getText();
        if (urlText.isEmpty()) {
            setErrorText(DQLBundle.message("settings.dql.tenants.form.error.missingUrl"));
            return;
        }

        try {
            URL ignored = URI.create(urlField.getText()).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            setErrorText(DQLBundle.message("settings.dql.tenants.form.error.invalidTenantUrl", e.getMessage()));
            return;
        }

        DynatraceTenant.AuthType selectedAuthType = (DynatraceTenant.AuthType) Objects.requireNonNullElse(authenticationMethod.getSelectedItem(), DynatraceTenant.AuthType.SSO_OAUTH);

        if (selectedAuthType == DynatraceTenant.AuthType.API_TOKEN) {
            if (!apiTokenPanel.hasToken()) {
                setErrorText(DQLBundle.message("settings.dql.tenants.form.error.missingToken"));
                return;
            }
            String credentialId = tenant != null && StringUtil.isNotEmpty(tenant.getCredentialId()) ?
                    tenant.getCredentialId() : UUID.randomUUID().toString();
            apiTokenPanel.saveToken(credentialId, name.getText());
            tenant = new DynatraceTenant(name.getText(), urlField.getText(), credentialId, DynatraceTenant.AuthType.API_TOKEN);
        } else {
            if (!oauthPanel.isAuthenticated()) {
                setErrorText(DQLBundle.message("settings.dql.tenants.form.error.oauthNotAuthenticated"));
                return;
            }
            String credentialId = oauthPanel.getCredentialId() != null ?
                    oauthPanel.getCredentialId() : UUID.randomUUID().toString();
            tenant = new DynatraceTenant(name.getText(), urlField.getText(), credentialId, DynatraceTenant.AuthType.SSO_OAUTH);
        }
        if (apiTokenPanel != null) {
            apiTokenPanel.dispose();
        }
        if (oauthPanel != null) {
            oauthPanel.dispose();
        }
        super.doOKAction();
    }
}

package pl.thedeem.intellij.dql.settings.tenants;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.components.LoadingPanel;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.services.dynatrace.DynatraceOAuthService;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class OAuthSignInPanel extends JBPanel<OAuthSignInPanel> implements Disposable {
    private static final Logger LOG = Logger.getInstance(OAuthSignInPanel.class);
    private final Supplier<String> urlSupplier;
    private final JButton signInButton;
    private final JButton signOutButton;
    private final BorderLayoutPanel statusPanel;
    private final LoadingPanel loadingPanel;

    private boolean authenticated;
    private String credentialId;
    private CompletableFuture<Void> pendingFlow;

    public OAuthSignInPanel(@NotNull Supplier<String> urlSupplier) {
        this.urlSupplier = urlSupplier;

        statusPanel = new BorderLayoutPanel();
        signInButton = new JButton(DQLBundle.message("settings.dql.tenants.form.oauth.signIn"), AllIcons.Actions.MoveToWindow);
        signInButton.setVisible(false);
        signOutButton = new JButton(DQLBundle.message("settings.dql.tenants.form.oauth.signOut"), AllIcons.Actions.Exit);
        signOutButton.setVisible(false);
        signInButton.addActionListener(e -> signIn());
        signOutButton.addActionListener(e -> signOut());
        loadingPanel = new LoadingPanel(DQLBundle.message("settings.dql.tenants.form.oauth.loading"));

        JBPanel<?> topPart = new JBPanel<>(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPart.add(signInButton);
        topPart.add(signOutButton);
        topPart.add(statusPanel);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(JBUI.Borders.empty());
        add(topPart);
    }

    public void init(@Nullable DynatraceTenant tenant) {
        if (tenant == null || tenant.getCredentialId() == null) {
            setAuthenticated(false);
            return;
        }
        this.credentialId = tenant.getCredentialId();
        if (credentialId == null) {
            return;
        }
        updateStatus(loadingPanel);
        DynatraceOAuthService.getInstance().resolveToken(credentialId, tenant.getUrl())
                .whenComplete((token, ex) -> {
                    if (ex != null) {
                        LOG.error("Critical failure resolving OAuth status for " + credentialId, ex);
                    }
                    ApplicationManager.getApplication().invokeLater(
                            () -> setAuthenticated(ex == null && token != null),
                            ModalityState.any()
                    );
                });
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public @Nullable String getCredentialId() {
        return credentialId;
    }

    @Override
    public void dispose() {
        loadingPanel.dispose();
        if (pendingFlow != null && !pendingFlow.isDone()) {
            pendingFlow.cancel(true);
        }
    }

    private void signIn() {
        String url = urlSupplier.get();
        this.credentialId = Objects.requireNonNullElse(credentialId, UUID.randomUUID().toString());

        signInButton.setEnabled(false);
        loadingPanel.setText(DQLBundle.message("settings.dql.tenants.form.oauth.authenticating"));
        updateStatus(loadingPanel);

        DynatraceOAuthService oauthService = DynatraceOAuthService.getInstance();
        pendingFlow = oauthService.signIn(url, credentialId);
        pendingFlow.whenComplete((ignored, ex) -> ApplicationManager.getApplication().invokeLater(() -> {
            signInButton.setEnabled(true);
            if (ex instanceof CancellationException) {
                setAuthenticated(false);
                return;
            }
            if (ex != null) {
                LOG.warn("OAuth authentication failed", ex);
                updateStatus(new InformationComponent(
                        DQLBundle.message("settings.dql.tenants.form.oauth.error", ex.getMessage()),
                        AllIcons.General.Error
                ));
                setAuthenticated(false);
                return;
            }
            setAuthenticated(true);
        }, ModalityState.stateForComponent(this)));
    }

    private void signOut() {
        if (credentialId != null) {
            DynatraceOAuthService.getInstance().signOut(credentialId)
                    .exceptionally(ex -> { LOG.warn("Failed to complete sign-out", ex); return null; });
        }
        setAuthenticated(false);
    }

    private void setAuthenticated(boolean isAuthenticated) {
        this.authenticated = isAuthenticated;
        if (isAuthenticated) {
            updateStatus(new InformationComponent(
                    DQLBundle.message("settings.dql.tenants.form.oauth.status.authenticated"),
                    AllIcons.General.GreenCheckmark
            ));
            signInButton.setVisible(false);
            signOutButton.setVisible(true);
        } else {
            updateStatus(new InformationComponent(
                    DQLBundle.message("settings.dql.tenants.form.oauth.status.notAuthenticated"),
                    AllIcons.General.Information
            ));
            signInButton.setVisible(true);
            signOutButton.setVisible(false);
        }
    }

    private void updateStatus(@NotNull JComponent component) {
        statusPanel.removeAll();
        statusPanel.addToCenter(component);
        statusPanel.revalidate();
        statusPanel.repaint();
    }
}

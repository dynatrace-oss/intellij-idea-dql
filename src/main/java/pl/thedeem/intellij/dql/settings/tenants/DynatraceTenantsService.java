package pl.thedeem.intellij.dql.settings.tenants;

import com.intellij.icons.AllIcons;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.sdk.errors.DTAuthException;
import pl.thedeem.intellij.common.sdk.errors.SSONotConfiguredException;
import pl.thedeem.intellij.common.sdk.errors.SSOReAuthRequiredException;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.services.dynatrace.DynatraceOAuthService;
import pl.thedeem.intellij.dql.services.notifications.DQLNotificationsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@State(name = "DynatraceTenantsConfig", storages = @Storage("dynatraceTenants.xml"))
public class DynatraceTenantsService implements PersistentStateComponent<DynatraceTenantsService.State> {
    private static final Logger LOG = Logger.getInstance(DynatraceTenantsService.class);
    public static class State {
        public List<DynatraceTenant> tenants = new ArrayList<>();
        public boolean missingTenantsNotificationDismissed;
    }

    private final State myState = new State();

    public static @NotNull DynatraceTenantsService getInstance() {
        return ApplicationManager.getApplication().getService(DynatraceTenantsService.class);
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        XmlSerializerUtil.copyBean(state, myState);
    }

    public List<DynatraceTenant> getTenants() {
        return myState.tenants;
    }

    public void addTenant(DynatraceTenant tenant) {
        myState.tenants.add(tenant);
    }

    public void removeTenant(DynatraceTenant tenant) {
        if (tenant.getCredentialId() != null) {
            PasswordSafe.getInstance().set(DQLUtil.createCredentialAttributes(tenant.getCredentialId()), null);
            if (tenant.getAuthType() == DynatraceTenant.AuthType.SSO_OAUTH) {
                DynatraceOAuthService.getInstance().signOut(tenant.getCredentialId())
                        .exceptionally(ex -> { LOG.warn("Failed to complete sign-out for tenant: " + tenant.getName(), ex); return null; });
            }
        }
        myState.tenants.removeIf(t -> Objects.equals(t.getName(), tenant.getName()));
    }

    public @Nullable String resolveApiToken(@NotNull Project project, @Nullable DynatraceTenant tenant) throws DTAuthException {
        if (tenant == null) {
            return null;
        }
        return switch (tenant.getAuthType()) {
            case API_TOKEN -> {
                String credentialId = tenant.getCredentialId();
                if (credentialId == null || credentialId.isEmpty()) {
                    yield null;
                }
                yield PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(credentialId));
            }
            case SSO_OAUTH -> {
                String credentialId = tenant.getCredentialId();
                if (credentialId == null || credentialId.isEmpty()) {
                    yield null;
                }
                try {
                    yield DynatraceOAuthService.getInstance().resolveToken(credentialId, tenant.getUrl()).get();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof SSOReAuthRequiredException re) {
                        DQLNotificationsService.getInstance(project).showNotification(
                                DQLNotificationsService.ERRORS,
                                DQLBundle.message("notifications.error.invalidAuth.title", tenant.getName()),
                                DQLBundle.message("notifications.error.oauthSessionExpired.message", tenant.getName()),
                                NotificationType.WARNING,
                                project,
                                List.of(new AnAction(DQLBundle.message("notifications.error.oauthSessionExpired.action"), null, AllIcons.Actions.ForceRefresh) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        DynatraceTenantsConfigurable.showSettings(tenant.getName());
                                    }
                                }));
                        throw re;
                    }
                    if (cause instanceof SSONotConfiguredException sso) {
                        DQLNotificationsService.getInstance(project).showNotification(
                                DQLNotificationsService.ERRORS,
                                DQLBundle.message("notifications.error.ssoNotConfigured.title", tenant.getName()),
                                DQLBundle.message("notifications.error.ssoNotConfigured.message", tenant.getName()),
                                NotificationType.WARNING,
                                project,
                                List.of(new AnAction(DQLBundle.message("notifications.error.oauthNotConfigured.action"), null, AllIcons.Actions.ForceRefresh) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e) {
                                        DynatraceTenantsConfigurable.showSettings(tenant.getName());
                                    }
                                }));
                        throw sso;
                    }
                    throw new DTAuthException("Unexpected error resolving OAuth token", cause);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new DTAuthException("Interrupted while resolving OAuth token", e);
                }
            }
        };
    }

    public @Nullable DynatraceTenant findTenant(@Nullable String tenant) {
        return getTenants().stream()
                .filter(t -> Objects.equals(tenant, t.getName()))
                .findFirst()
                .orElse(null);
    }

    public boolean isMissingTenantsNotificationDismissed() {
        return myState.missingTenantsNotificationDismissed;
    }

    public void dismissMissingTenantsNotification() {
        myState.missingTenantsNotificationDismissed = true;
    }
}
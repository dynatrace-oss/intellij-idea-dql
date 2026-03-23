package pl.thedeem.intellij.dql.settings.tenants;

import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.LoadingPanel;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;

import java.util.Arrays;

public class ApiTokenPanel extends BorderLayoutPanel implements Disposable {
    private final JBPasswordField passwordField = new JBPasswordField();
    private final LoadingPanel loading;

    public ApiTokenPanel() {
        loading = new LoadingPanel(DQLBundle.message("settings.dql.tenants.form.passwordLoading"));
        addToCenter(loading);
    }

    public void init(@Nullable DynatraceTenant tenant) {
        if (tenant == null) {
            updateUI(null);
            return;
        }
        String credentialId = tenant.getCredentialId();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            String storedToken = StringUtil.isNotEmpty(credentialId) ?
                    PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(credentialId)) : null;
            ApplicationManager.getApplication().invokeLater(() -> updateUI(storedToken), ModalityState.any());
        });
    }

    private void updateUI(@Nullable String token) {
        removeAll();
        passwordField.setText(token);
        addToTop(passwordField);
        revalidate();
        repaint();
    }

    public boolean hasToken() {
        return passwordField.getPassword().length > 0;
    }

    public void saveToken(@NotNull String credentialId, @NotNull String name) {
        PasswordSafe.getInstance().set(
                DQLUtil.createCredentialAttributes(credentialId),
                new Credentials(name, passwordField.getPassword())
        );
    }

    @Override
    public void dispose() {
        Arrays.fill(passwordField.getPassword(), ' ');
        passwordField.setText("");
        loading.dispose();
    }
}


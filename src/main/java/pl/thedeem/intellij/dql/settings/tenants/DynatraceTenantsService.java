package pl.thedeem.intellij.dql.settings.tenants;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.executing.DQLExecutionUtil;
import pl.thedeem.intellij.dql.executing.executeDql.runConfiguration.ExecuteDQLRunConfiguration;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@State(name = "DynatraceTenantsConfig", storages = @Storage("dynatraceTenants.xml"))
public class DynatraceTenantsService implements PersistentStateComponent<DynatraceTenantsService.State> {
    public static class State {
        public List<DynatraceTenant> tenants = new ArrayList<>();
    }

    private final State myState = new State();

    public static DynatraceTenantsService getInstance() {
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
        }
        myState.tenants.removeIf(t -> Objects.equals(t.getName(), tenant.getName()));
    }

    public @Nullable DynatraceTenant findTenant(@NotNull String tenant) {
        return getTenants().stream()
                .filter(t -> tenant.equals(t.getName()))
                .findFirst()
                .orElse(null);
    }

    public String findTenantName(@NotNull Project project, @NotNull PsiFile psiFile) {
        RunnerAndConfigurationSettings existingSettings = DQLExecutionUtil.findExistingSettings(project, psiFile);
        if (existingSettings != null && existingSettings.getConfiguration() instanceof ExecuteDQLRunConfiguration executeDQL) {
            String tenant = executeDQL.getTenantName();
            if (StringUtil.isNotEmpty(tenant)) {
                return tenant;
            }
        }
        return DQLSettings.getInstance().getDefaultDynatraceTenant();
    }
}
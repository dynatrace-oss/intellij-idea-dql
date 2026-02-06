package pl.thedeem.intellij.dql.services.ui;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.services.ManagedServiceGroup;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.util.Objects;

public class TenantServiceGroup implements ManagedServiceGroup {
    private final String tenantId;

    public TenantServiceGroup(@NotNull String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        DynatraceTenant tenant = DynatraceTenantsService.getInstance().findTenant(tenantId);
        return new StandardItemPresentation(tenant != null ? tenant.getName() : tenantId, null, DQLIcon.DYNATRACE_LOGO);
    }

    @Override
    public void dispose() {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TenantServiceGroup that = (TenantServiceGroup) o;
        return Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tenantId);
    }
}

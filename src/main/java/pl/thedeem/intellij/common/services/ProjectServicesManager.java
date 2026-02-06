package pl.thedeem.intellij.common.services;

import com.intellij.execution.services.ServiceEventListener;
import com.intellij.execution.services.ServiceEventListener.ServiceEvent;
import com.intellij.execution.services.ServiceViewManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service(Service.Level.PROJECT)
public final class ProjectServicesManager implements Disposable {
    private final List<ManagedService> children;

    private final Project project;

    public ProjectServicesManager(@NotNull Project project) {
        this.project = project;
        this.children = new ArrayList<>();
    }

    public static @NotNull ProjectServicesManager getInstance(@NotNull Project project) {
        return project.getService(ProjectServicesManager.class);
    }

    public @NotNull List<ManagedService> getRegisteredServices() {
        return children;
    }

    @Override
    public void dispose() {
        for (ManagedService service : children) {
            service.dispose();
        }
        children.clear();
    }

    public <T extends ManagedService> void registerService(@NotNull T service) {
        unregisterService(service);
        children.add(service);
        project.getMessageBus().syncPublisher(ServiceEventListener.TOPIC).handle(
                ServiceEventListener.ServiceEvent.createServiceAddedEvent(service, ManagedServiceViewContributor.class, null)
        );
        ServiceViewManager instance = ServiceViewManager.getInstance(project);
        instance.select(service, ManagedServiceViewContributor.class, true, true);
    }

    public <T extends ManagedService> void unregisterService(@NotNull T service) {
        ManagedService existing = children.stream().filter(e -> e.equals(service)).findFirst().orElse(null);
        if (existing != null) {
            existing.dispose();
            children.remove(existing);
            project.getMessageBus().syncPublisher(ServiceEventListener.TOPIC).handle(
                    ServiceEvent.createEvent(ServiceEventListener.EventType.SERVICE_REMOVED, existing, ManagedServiceViewContributor.class)
            );
        }
    }

    public @NotNull List<ManagedService> find(@NotNull Predicate<? super ManagedService> filter) {
        return children.stream().filter(filter).toList();
    }
}

package pl.thedeem.intellij.dql.services.ui;

import com.intellij.execution.services.ServiceEventListener;
import com.intellij.execution.services.ServiceEventListener.ServiceEvent;
import com.intellij.execution.services.ServiceViewManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service(Service.Level.PROJECT)
public final class DQLServicesManager implements Disposable {
    private final Project project;
    private final List<DQLManagedService<?>> services = new ArrayList<>();

    public DQLServicesManager(@NotNull Project project) {
        this.project = project;
    }

    public static DQLServicesManager getInstance(@NotNull Project project) {
        return project.getService(DQLServicesManager.class);
    }

    private String getServiceId(@NotNull DQLManagedService<?> service) {
        return service.getPresentation().getPresentableText();
    }

    public @Nullable DQLManagedService<?> getService(String name) {
        return services.stream().filter(s -> getServiceId(s).equals(name)).findFirst().orElse(null);
    }

    public <T> void startExecution(@NotNull DQLManagedService<T> service, @NotNull T params) {
        stopExecution(service);
        services.add(service);
        ServiceViewManager instance = ServiceViewManager.getInstance(project);
        instance.select(service, DQLServiceViewContributor.class, true, true);
        project.getMessageBus().syncPublisher(ServiceEventListener.TOPIC).handle(
                ServiceEvent.createServiceAddedEvent(service, DQLServiceViewContributor.class, null)
        );
        ApplicationManager.getApplication().executeOnPooledThread(() -> service.startExecution(params));
    }

    public void stopExecution(@NotNull DQLManagedService<?> service) {
        DQLManagedService<?> existing = getService(getServiceId(service));
        if (existing != null) {
            existing.stopExecution();
            services.remove(existing);
            project.getMessageBus().syncPublisher(ServiceEventListener.TOPIC).handle(
                    ServiceEvent.createEvent(ServiceEventListener.EventType.SERVICE_REMOVED, existing, DQLServiceViewContributor.class)
            );
        }
    }

    public void refreshExecution(@NotNull DQLManagedService<?> service) {
        DQLManagedService<?> existing = getService(getServiceId(service));
        if (existing != null) {
            project.getMessageBus().syncPublisher(ServiceEventListener.TOPIC).handle(
                    ServiceEvent.createEvent(ServiceEventListener.EventType.SERVICE_CHANGED, existing, DQLServiceViewContributor.class)
            );
        }
    }

    public @NotNull List<DQLManagedService<?>> getActiveServices() {
        return services;
    }

    @Override
    public void dispose() {
        services.clear();
    }

    public List<DQLManagedService<?>> findServices(@NotNull Predicate<DQLManagedService<?>> filter) {
        return this.services.stream().filter(filter).toList();
    }
}

package pl.thedeem.intellij.dql.executing.services;

import com.intellij.execution.services.ServiceEventListener;
import com.intellij.execution.services.ServiceEventListener.ServiceEvent;
import com.intellij.execution.services.ServiceViewManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.executing.executeDql.DQLExecutionService;

import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class DQLServicesManager implements Disposable {
    private final Project project;
    private final List<DQLExecutionService> services = new ArrayList<>();

    public DQLServicesManager(@NotNull Project project) {
        this.project = project;
    }

    public static DQLServicesManager getInstance(@NotNull Project project) {
        return project.getService(DQLServicesManager.class);
    }

    private String getServiceId(@NotNull DQLExecutionService service) {
        return service.getPresentation().getPresentableText();
    }

    public @Nullable DQLExecutionService getService(String name) {
        return services.stream().filter(s -> getServiceId(s).equals(name)).findFirst().orElse(null);
    }

    public void startExecution(@NotNull DQLExecutionService service) {
        stopExecution(service);
        services.add(service);
        ServiceViewManager instance = ServiceViewManager.getInstance(project);
        instance.select(service, DQLServiceViewContributor.class, true, true);
        project.getMessageBus().syncPublisher(ServiceEventListener.TOPIC).handle(
                ServiceEvent.createServiceAddedEvent(service, DQLServiceViewContributor.class, null)
        );
    }

    public void stopExecution(@NotNull DQLExecutionService service) {
        DQLExecutionService existing = getService(getServiceId(service));
        if (existing != null) {
            services.remove(existing);
            project.getMessageBus().syncPublisher(ServiceEventListener.TOPIC).handle(
                    ServiceEvent.createEvent(ServiceEventListener.EventType.SERVICE_REMOVED, existing, DQLServiceViewContributor.class)
            );
        }
    }

    public @NotNull List<DQLExecutionService> getActiveServices() {
        return services;
    }

    @Override
    public void dispose() {
        services.clear();
    }
}

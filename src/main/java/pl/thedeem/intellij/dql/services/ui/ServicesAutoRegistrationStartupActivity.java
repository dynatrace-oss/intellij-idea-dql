package pl.thedeem.intellij.dql.services.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.services.ProjectServicesManager;

public final class ServicesAutoRegistrationStartupActivity implements ProjectActivity {
    @Override
    public @NotNull Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        ProjectServicesManager.getInstance(project).registerService(ConnectedTenantsServiceGroup.getInstance());
        return Unit.INSTANCE;
    }
}

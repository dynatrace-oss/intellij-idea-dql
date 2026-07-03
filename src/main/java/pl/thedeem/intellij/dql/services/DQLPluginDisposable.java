package pl.thedeem.intellij.dql.services;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.PROJECT)
public final class DQLPluginDisposable implements Disposable {
    public static @NotNull Disposable getInstance(@NotNull Project project) {
        return project.getService(DQLPluginDisposable.class);
    }

    @Override
    public void dispose() {
    }
}

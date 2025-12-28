package pl.thedeem.intellij.dql.services.query;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface DQLFieldNamesService {
    static @NotNull DQLFieldNamesService getInstance(@NotNull Project project) {
        return project.getService(DQLFieldNamesService.class);
    }

    @NotNull String calculateFieldName(@Nullable Object... parts);

    record SeparatedChildren(@NotNull Collection<?> children, @Nullable Object separator) {
    }
}

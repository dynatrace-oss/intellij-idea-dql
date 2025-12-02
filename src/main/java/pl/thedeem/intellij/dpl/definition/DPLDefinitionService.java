package pl.thedeem.intellij.dpl.definition;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;

import java.util.Map;

public interface DPLDefinitionService {
    static @NotNull DPLDefinitionService getInstance(@NotNull Project project) {
        return project.getService(DPLDefinitionService.class);
    }

    void invalidateCache();

    @NotNull Map<String, String> posixGroups();

    @NotNull Map<String, ExpressionDescription> commands();

    @NotNull Map<String, ExpressionDescription> expressions();
}

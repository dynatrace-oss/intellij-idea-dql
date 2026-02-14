package pl.thedeem.intellij.dpl.definition;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.DefinitionUtils;
import pl.thedeem.intellij.dpl.definition.model.DPLDefinition;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DPLDefinitionServiceImpl implements DPLDefinitionService {
    private static final String DEFINITION_FILE = "definition/dpl.json";
    private static final Logger logger = Logger.getInstance(DPLDefinitionServiceImpl.class);

    private final Project project;
    private final SimpleModificationTracker tracker;
    private CachedValue<DPLDefinition> definition;
    private CachedValue<Map<String, ExpressionDescription>> commands;

    public DPLDefinitionServiceImpl(@NotNull Project project) {
        this.project = project;
        this.tracker = new SimpleModificationTracker();
    }

    @Override
    public void invalidateCache() {
        tracker.incModificationCount();
    }

    @Override
    public @NotNull Map<String, String> posixGroups() {
        return getDefinition().posix();
    }

    @Override
    public @NotNull Map<String, ExpressionDescription> commands() {
        if (this.commands == null) {
            commands = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loadCommands(), tracker),
                    false
            );
        }
        return this.commands.getValue();
    }

    @Override
    public @NotNull Map<String, ExpressionDescription> expressions() {
        return getDefinition().expressions();
    }

    private @NotNull DPLDefinition getDefinition() {
        if (this.definition == null) {
            definition = CachedValuesManager.getManager(project).createCachedValue(
                    () -> new CachedValueProvider.Result<>(loadDefinition(), tracker),
                    false
            );
        }
        return this.definition.getValue();
    }

    private @NotNull DPLDefinition loadDefinition() {
        return Objects.requireNonNullElse(
                DefinitionUtils.loadDefinitionFromFile(DEFINITION_FILE, DPLDefinition.class),
                DPLDefinition.empty()
        );
    }

    private @NotNull Map<String, ExpressionDescription> loadCommands() {
        logger.info("Reloading commands");
        Map<String, ExpressionDescription> result = new HashMap<>();
        for (ExpressionDescription description : getDefinition().commands().values()) {
            result.put(description.name().toUpperCase(), description);
            if (description.aliases() != null) {
                for (String alias : description.aliases()) {
                    result.put(alias.toUpperCase(), description);
                }
            }
        }
        return result;
    }
}

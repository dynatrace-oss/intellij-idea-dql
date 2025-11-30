package pl.thedeem.intellij.dpl.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dpl.definition.model.DPLDefinition;
import pl.thedeem.intellij.dpl.definition.model.ExpressionDescription;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DPLDefinitionServiceImpl implements DPLDefinitionService {
    private static final String DEFINITION_FILE = "/definition/dpl.json";
    private static final Logger logger = Logger.getInstance(DPLDefinitionServiceImpl.class);
    private static final ObjectMapper mapper = new ObjectMapper();

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
        logger.info("Loading DPL definition from file: " + DEFINITION_FILE);
        try (InputStream inputStream = getClass().getResourceAsStream(DEFINITION_FILE)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Definition file not found: " + DEFINITION_FILE);
            }
            return mapper.readValue(inputStream, DPLDefinition.class);
        } catch (IOException error) {
            logger.warn("Failed to load command definitions from " + DEFINITION_FILE, error);
        }
        return DPLDefinition.empty();
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

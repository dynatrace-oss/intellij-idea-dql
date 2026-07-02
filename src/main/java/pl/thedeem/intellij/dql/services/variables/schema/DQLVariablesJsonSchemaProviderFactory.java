package pl.thedeem.intellij.dql.services.variables.schema;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory;
import com.jetbrains.jsonSchema.extension.SchemaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;

import java.util.List;

public final class DQLVariablesJsonSchemaProviderFactory implements JsonSchemaProviderFactory {
    private static final String SCHEMA_RESOURCE = "/schemas/dql-variables.schema.json";

    @Override
    public @NotNull List<JsonSchemaFileProvider> getProviders(@NotNull Project project) {
        return List.of(new DQLVariablesJsonSchemaFileProvider());
    }

    private static final class DQLVariablesJsonSchemaFileProvider implements JsonSchemaFileProvider {
        @Override
        public boolean isAvailable(@NotNull VirtualFile file) {
            return DQLVariablesService.DQL_VARIABLES_FILE.equals(file.getName());
        }

        @Override
        public @NotNull String getName() {
            return "DQL Variables";
        }

        @Override
        public @Nullable VirtualFile getSchemaFile() {
            return JsonSchemaProviderFactory.getResourceFile(DQLVariablesJsonSchemaProviderFactory.class, SCHEMA_RESOURCE);
        }

        @Override
        public @NotNull SchemaType getSchemaType() {
            return SchemaType.embeddedSchema;
        }
    }
}

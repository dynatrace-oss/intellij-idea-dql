package pl.thedeem.intellij.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public class DefinitionUtils {
    private static final ObjectMapper mapper = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    private static final Logger logger = Logger.getInstance(DefinitionUtils.class);

    public @Nullable
    static <T> T loadDefinitionFromFile(String filePath, Class<T> ref) {
        try (InputStream inputStream = DefinitionUtils.class.getClassLoader().getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Definition file not found: " + filePath);
            }
            return mapper.readValue(inputStream, new TypeReference<>() {
                @Override
                public Type getType() {
                    return ref;
                }
            });
        } catch (IOException error) {
            logger.warn("Failed to load command definitions from " + filePath, error);
        }
        return null;
    }

    public @NotNull
    static <T> T mergeDefinitions(@NotNull T original, @Nullable String overrides) {
        if (overrides == null) {
            return original;
        }
        try (InputStream overridesStream = DefinitionUtils.class.getClassLoader().getResourceAsStream(overrides)) {
            if (overridesStream == null) {
                return original;
            }
            ObjectReader reader = mapper.readerForUpdating(original);
            JsonNode override = mapper.readTree(overridesStream);
            return reader.readValue(override);
        } catch (IOException e) {
            logger.warn("Failed to merge definitions from " + original, e);
            return original;
        }
    }
}

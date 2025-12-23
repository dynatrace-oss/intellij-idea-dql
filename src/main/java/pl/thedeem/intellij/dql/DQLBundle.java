package pl.thedeem.intellij.dql;

import com.intellij.DynamicBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.services.definition.DQLDefinitionService;
import pl.thedeem.intellij.dql.definition.model.DataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class DQLBundle extends DynamicBundle {
    private static final int MAX_STRING_LENGTH = 25;

    @NonNls
    public static final String BUNDLE = "messages.DQLBundle";
    private static final DQLBundle INSTANCE = new DQLBundle(DQLBundle.class, BUNDLE);

    public DQLBundle(@NotNull Class<?> bundleClass, @NotNull String pathToBundle) {
        super(bundleClass, pathToBundle);
    }

    @NotNull
    public static @Nls String message(@NotNull @NonNls String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }

    @NotNull
    public static @Nls String shorten(String text) {
        return StringUtil.shortenPathWithEllipsis(text, MAX_STRING_LENGTH, true);
    }

    @NotNull
    public static @Nls String types(@Nullable Collection<String> dataTypes, @NotNull Project project) {
        if (dataTypes == null) {
            return DQLBundle.message("generic.noTypes");
        }
        DQLDefinitionService service = DQLDefinitionService.getInstance(project);
        List<String> types = dataTypes.stream().map(service::findDataType).filter(Objects::nonNull).map(DataType::name).toList();
        return print(types, DQLBundle.message("generic.noTypes"));
    }

    @NotNull
    public static @Nls String print(@Nullable Collection<?> collection) {
        return print(collection, "");
    }

    public static @Nls String print(@Nullable Collection<?> collection, @NotNull String defaultValue) {
        if (collection == null || collection.isEmpty()) {
            return defaultValue;
        }
        List<String> list = new ArrayList<>(collection.stream().map(Object::toString).toList());
        if (list.size() == 1) {
            return list.getFirst();
        }
        String last = list.removeLast();
        return message("generic.lists", String.join(", ", list), last);
    }
}

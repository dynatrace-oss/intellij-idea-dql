package pl.thedeem.intellij.dpl;

import com.intellij.DynamicBundle;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DPLBundle extends DynamicBundle {
    private static final int MAX_STRING_LENGTH = 25;

    @NonNls
    public static final String BUNDLE = "messages.DPLBundle";
    private static final DPLBundle INSTANCE = new DPLBundle(DPLBundle.class, BUNDLE);

    public DPLBundle(@NotNull Class<?> bundleClass, @NotNull String pathToBundle) {
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
    public static @Nls String print(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }
        List<String> list = new ArrayList<>(collection.stream().map(Object::toString).toList());
        if (list.size() == 1) {
            return list.getFirst();
        }
        String last = list.removeLast();
        return message("generic.lists", String.join(", ", list), last);
    }
}

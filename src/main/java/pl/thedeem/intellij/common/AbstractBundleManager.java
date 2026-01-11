package pl.thedeem.intellij.common;

import com.intellij.DynamicBundle;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractBundleManager extends DynamicBundle {
    private static final int MAX_STRING_LENGTH = 25;

    protected AbstractBundleManager(@NotNull Class<?> bundleClass, @NotNull String pathToBundle) {
        super(bundleClass, pathToBundle);
    }

    protected @NotNull @Nls String shortenString(String text) {
        return StringUtil.shortenPathWithEllipsis(text, MAX_STRING_LENGTH, true);
    }

    protected @NotNull @Nls String printCollection(Collection<?> collection, @NotNull String defaultValue) {
        if (collection == null || collection.isEmpty()) {
            return defaultValue;
        }
        List<String> list = new ArrayList<>(collection.stream().map(Object::toString).toList());
        if (list.size() == 1) {
            return list.getFirst();
        }
        String last = list.removeLast();
        return getMessage("generic.lists", String.join(", ", list), last);
    }
}

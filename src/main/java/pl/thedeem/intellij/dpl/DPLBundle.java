package pl.thedeem.intellij.dpl;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.AbstractBundleManager;

import java.util.Collection;

public class DPLBundle extends AbstractBundleManager {
    @NonNls
    public static final String BUNDLE = "messages.DPLBundle";
    private static final DPLBundle INSTANCE = new DPLBundle(DPLBundle.class, BUNDLE);

    public DPLBundle(@NotNull Class<?> bundleClass, @NotNull String pathToBundle) {
        super(bundleClass, pathToBundle);
    }

    public static @NotNull @Nls String message(@NotNull @NonNls String key, Object @NotNull ... params) {
        return INSTANCE.getMessage(key, params);
    }

    public static @NotNull @Nls String print(Collection<?> collection) {
        return INSTANCE.printCollection(collection, "");
    }
}

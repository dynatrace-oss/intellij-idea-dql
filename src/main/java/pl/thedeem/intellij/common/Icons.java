package pl.thedeem.intellij.common;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface Icons {
    Icon DYNATRACE_LOGO = IconLoader.getIcon("/icons/dynatrace.svg", Icons.class);

    static Icon layer(@NotNull Icon base, @NotNull Icon badge, int position) {
        LayeredIcon layeredIcon = new LayeredIcon(2);
        layeredIcon.setIcon(base, 0);
        layeredIcon.setIcon(badge, 1, position);
        return layeredIcon;
    }

    static Icon layerScaled(@NotNull Icon base, @NotNull Icon badge, float scale, int position) {
        Icon scaled = IconUtil.scale(badge, null, scale);
        return layer(base, scaled, position);
    }
}

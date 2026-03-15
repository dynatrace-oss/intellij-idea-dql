package pl.thedeem.intellij.common;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface Icons {
    Icon DYNATRACE_LOGO = IconLoader.getIcon("/icons/dynatrace.svg", Icons.class);

    static Icon scaleToBottomRight(@NotNull Icon base, @NotNull Icon badge, float scale) {
        LayeredIcon layeredIcon = new LayeredIcon(2);
        layeredIcon.setIcon(base, 0);

        Icon scaled = IconUtil.scale(badge, null, scale);
        layeredIcon.setIcon(scaled, 1, SwingConstants.SOUTH_EAST);

        return layeredIcon;
    }
}

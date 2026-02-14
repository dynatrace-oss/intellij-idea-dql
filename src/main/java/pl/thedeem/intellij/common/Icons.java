package pl.thedeem.intellij.common;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface Icons {
    Icon DYNATRACE_LOGO = IconLoader.getIcon("/icons/dynatrace.png", Icons.class);

    static Icon scaleToBottomRight(@NotNull Icon base, @NotNull Icon original, float scale) {
        Icon scaled = IconUtil.scale(original, null, scale);

        return new LayeredIcon(2) {{
            setIcon(base, 0);
            setIcon(
                    scaled,
                    1,
                    base.getIconWidth() - scaled.getIconWidth(),
                    base.getIconHeight() - scaled.getIconHeight()
            );
        }};
    }
}

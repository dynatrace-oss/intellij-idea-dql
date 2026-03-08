package pl.thedeem.intellij.common;

import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface Icons {
    Icon DYNATRACE_LOGO = IconLoader.getIcon("/icons/dynatrace.svg", Icons.class);

    Icon LINE_CHART = IconLoader.getIcon("/icons/lineChart.svg", Icons.class);
    Icon BAR_CHART = IconLoader.getIcon("/icons/barChart.svg", Icons.class);
    Icon PIE_CHART = IconLoader.getIcon("/icons/pieChart.svg", Icons.class);
    Icon AREA_CHART = IconLoader.getIcon("/icons/areaChart.svg", Icons.class);

    Icon LEGEND_SHOW = IconLoader.getIcon("/icons/legendShow.svg", Icons.class);
    Icon LEGEND_HIDE = IconLoader.getIcon("/icons/legendHide.svg", Icons.class);
    Icon LEGEND_HIDE_OTHERS = IconLoader.getIcon("/icons/legendHideOthers.svg", Icons.class);
    Icon LEGEND_SHOW_ALL = IconLoader.getIcon("/icons/legendShowAll.svg", Icons.class);

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

package pl.thedeem.intellij.common;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public interface Icons {
    Icon DYNATRACE_LOGO = IconLoader.getIcon("/icons/dynatrace.svg", Icons.class);

    Icon LINE_CHART = IconLoader.getIcon("/icons/lineChart.svg", Icons.class);
    Icon BAR_CHART = IconLoader.getIcon("/icons/barChart.svg", Icons.class);
    Icon PIE_CHART = IconLoader.getIcon("/icons/pieChart.svg", Icons.class);

    Icon LEGEND_SHOW = IconLoader.getIcon("/icons/legendShow.svg", Icons.class);
    Icon LEGEND_HIDE = IconLoader.getIcon("/icons/legendHide.svg", Icons.class);
    Icon LEGEND_HIDE_OTHERS = IconLoader.getIcon("/icons/legendHideOthers.svg", Icons.class);
    Icon LEGEND_SHOW_ALL = IconLoader.getIcon("/icons/legendShowAll.svg", Icons.class);

    static Icon scaleToBottomRight(@NotNull Icon base, @NotNull Icon original, float scale) {
        Icon scaled = IconUtil.scale(original, null, scale);
        int badgeX = base.getIconWidth() - scaled.getIconWidth();
        int badgeY = base.getIconHeight() - scaled.getIconHeight();
        int border = JBUI.scale(1);

        return new Icon() {
            private BufferedImage cachedImage;
            private float cachedPixelScale;

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                float pixelScale = JBUI.pixScale(c);
                if (cachedImage == null || Float.compare(cachedPixelScale, pixelScale) != 0) {
                    cachedPixelScale = pixelScale;
                    BufferedImage image = UIUtil.createImage(c, getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = image.createGraphics();
                    try {
                        base.paintIcon(c, g2d, 0, 0);
                        g2d.setComposite(AlphaComposite.Clear);
                        g2d.fillOval(
                                badgeX - border,
                                badgeY - border,
                                scaled.getIconWidth() + 2 * border,
                                scaled.getIconHeight() + 2 * border
                        );
                        g2d.setComposite(AlphaComposite.SrcOver);
                        scaled.paintIcon(c, g2d, badgeX, badgeY);
                    } finally {
                        g2d.dispose();
                    }
                    cachedImage = image;
                }
                g.drawImage(cachedImage, x, y, c);
            }

            @Override
            public int getIconWidth() {
                return base.getIconWidth();
            }

            @Override
            public int getIconHeight() {
                return base.getIconHeight();
            }
        };
    }
}

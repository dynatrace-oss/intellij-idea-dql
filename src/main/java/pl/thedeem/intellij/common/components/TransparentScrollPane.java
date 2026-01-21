package pl.thedeem.intellij.common.components;

import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class TransparentScrollPane extends JBScrollPane {
    public TransparentScrollPane(@NotNull Component view) {
        super(view);
        initialize();
    }

    public TransparentScrollPane(@NotNull Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
        initialize();
    }

    private void initialize() {
        setBorder(JBUI.Borders.empty());
        setOpaque(false);
        getViewport().setOpaque(false);
    }
}

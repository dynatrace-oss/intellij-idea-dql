package pl.thedeem.intellij.common.components;

import com.intellij.openapi.Disposable;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class LoadingPanel extends BorderLayoutPanel implements Disposable {
    private final AsyncProcessIcon processIcon;
    private final JBLabel info;

    public LoadingPanel(@Nullable String text) {
        super();
        withBorder(JBUI.Borders.empty()).andTransparent();
        info = new JBLabel();
        if (text != null) {
            info.setText(text);
        }
        JBPanel<?> spinner = new JBPanel<>(new GridBagLayout())
                .withBorder(JBUI.Borders.empty())
                .andTransparent();
        processIcon = new AsyncProcessIcon("Preparing");
        spinner.add(processIcon);
        spinner.add(info);
        addToCenter(spinner);
    }

    public void setText(@NotNull String text) {
        info.setText(text);
    }

    @Override
    public void dispose() {
        processIcon.suspend();
        processIcon.dispose();
    }
}

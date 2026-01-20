package pl.thedeem.intellij.common.components;

import com.intellij.openapi.Disposable;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class LoadingPanel extends BorderLayoutPanel implements Disposable {
    private final AsyncProcessIcon processIcon;

    public LoadingPanel(@Nullable String text) {
        super();
        setOpaque(false);
        setBorder(JBUI.Borders.empty());
        JPanel spinner = new JPanel(new GridBagLayout());
        spinner.setOpaque(false);
        spinner.setBorder(JBUI.Borders.empty());
        processIcon = new AsyncProcessIcon("Preparing");
        spinner.add(processIcon);
        if (text != null) {
            spinner.add(new JBLabel(text));
        }
        addToCenter(spinner);
    }

    @Override
    public void dispose() {
        processIcon.suspend();
        processIcon.dispose();
    }
}

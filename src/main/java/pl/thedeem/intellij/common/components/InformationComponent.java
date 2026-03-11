package pl.thedeem.intellij.common.components;

import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class InformationComponent extends JBPanel<InformationComponent> {
    public InformationComponent(@NotNull String message, @NotNull Icon icon) {
        super(new GridBagLayout());
        withBorder(JBUI.Borders.empty()).andTransparent();
        JBLabel label = new JBLabel(message, icon, JLabel.LEFT);
        label.setCopyable(true);
        add(label);
    }
}

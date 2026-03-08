package pl.thedeem.intellij.common.components;

import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class InformationComponent extends JPanel {
    public InformationComponent(@NotNull String message, @NotNull Icon icon) {
        setLayout(new GridBagLayout());
        setBorder(JBUI.Borders.empty());
        setOpaque(false);
        JBLabel label = new JBLabel(message, icon, JLabel.LEFT);
        label.setCopyable(true);
        add(label);
    }
}

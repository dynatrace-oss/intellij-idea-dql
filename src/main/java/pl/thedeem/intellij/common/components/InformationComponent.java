package pl.thedeem.intellij.common.components;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class InformationComponent extends JPanel {
    public InformationComponent(@NotNull String message, @NotNull Icon icon) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        add(new JLabel(message, icon, JLabel.LEFT));
    }
}

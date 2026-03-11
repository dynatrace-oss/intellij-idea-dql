package pl.thedeem.intellij.common.components.simple;

import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GroupedSettingsComponent extends JBPanel<GroupedSettingsComponent> {
    private final JBPanel<?> children;

    public GroupedSettingsComponent(@NotNull String title) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        withBorder(JBUI.Borders.empty()).andTransparent();
        add(new TitledSeparator(title));

        children = new JBPanel<>().withBorder(JBUI.Borders.empty(JBUI.scale(10), 0));
        children.setLayout(new BoxLayout(children, BoxLayout.Y_AXIS));
        add(children);
    }

    public GroupedSettingsComponent addSetting(@NotNull JComponent setting) {
        children.add(setting);
        return this;
    }
}

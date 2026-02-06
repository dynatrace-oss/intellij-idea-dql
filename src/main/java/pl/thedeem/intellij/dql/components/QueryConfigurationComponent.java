package pl.thedeem.intellij.dql.components;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.components.ResizableTextField;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;

public class QueryConfigurationComponent extends JPanel {
    private final ResizableTextField scanLimit;
    private final ResizableTextField maxBytes;
    private final ResizableTextField maxRecords;

    public QueryConfigurationComponent() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 0, 2));
        setOpaque(false);
        setBorder(JBUI.Borders.empty());

        scanLimit = ResizableTextField.createNumericField(
                DQLBundle.message("action.DQL.QueryConfigurationAction.scanLimit.placeholder"),
                DQLBundle.message("action.DQLExecutionManagerToolbar.option.scanLimit")
        );
        maxBytes = ResizableTextField.createNumericField(
                DQLBundle.message("action.DQL.QueryConfigurationAction.maxBytes.placeholder"),
                DQLBundle.message("action.DQLExecutionManagerToolbar.option.maxBytes")
        );
        maxRecords = ResizableTextField.createNumericField(
                DQLBundle.message("action.DQL.QueryConfigurationAction.maxRecords.placeholder"),
                DQLBundle.message("action.DQLExecutionManagerToolbar.option.maxRecords")
        );
        add(configureField(scanLimit, AllIcons.Actions.GroupByModule));
        add(configureField(maxBytes, AllIcons.Actions.GroupByModuleGroup));
        add(configureField(maxRecords, AllIcons.Json.Array));
    }

    public ResizableTextField scanLimit() {
        return scanLimit;
    }

    public ResizableTextField maxBytes() {
        return maxBytes;
    }

    public ResizableTextField maxRecords() {
        return maxRecords;
    }

    private @NotNull JComponent configureField(@NotNull ResizableTextField field, @NotNull Icon icon) {
        field.setOpaque(false);
        JBLabel iconLabel = new JBLabel(icon);
        iconLabel.setToolTipText(field.getToolTipText());
        iconLabel.setBorder(JBUI.Borders.empty());
        iconLabel.setOpaque(false);
        JComponent wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(JBUI.Borders.empty());
        wrapper.add(iconLabel);
        wrapper.add(field);
        return wrapper;
    }
}

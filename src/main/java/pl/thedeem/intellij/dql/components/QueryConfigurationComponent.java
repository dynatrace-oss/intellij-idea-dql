package pl.thedeem.intellij.dql.components;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.ResizableTextField;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;

public class QueryConfigurationComponent extends JPanel {
    private final ResizableTextField scanLimit;
    private final ResizableTextField maxBytes;
    private final ResizableTextField maxRecords;

    public QueryConfigurationComponent() {
        this(false);
    }

    public QueryConfigurationComponent(boolean withLabels) {
        setOpaque(false);
        setBorder(JBUI.Borders.empty());

        scanLimit = ResizableTextField.createNumericField(
                DQLBundle.message("components.queryConfiguration.options.scanLimit.placeholder"),
                DQLBundle.message("components.queryConfiguration.options.scanLimit.tooltip")
        );
        maxBytes = ResizableTextField.createNumericField(
                DQLBundle.message("components.queryConfiguration.options.maxBytes.placeholder"),
                DQLBundle.message("components.queryConfiguration.options.maxBytes.tooltip")
        );
        maxRecords = ResizableTextField.createNumericField(
                DQLBundle.message("components.queryConfiguration.options.maxRecords.placeholder"),
                DQLBundle.message("components.queryConfiguration.options.maxRecords.tooltip")
        );
        add(configureField(scanLimit, AllIcons.Actions.GroupByModule, withLabels));
        add(configureField(maxBytes, AllIcons.Actions.GroupByModuleGroup, withLabels));
        add(configureField(maxRecords, AllIcons.Json.Array, withLabels));
    }

    public QueryConfigurationComponent configureFields(@NotNull Consumer<JBTextField> configurator) {
        configurator.consume(scanLimit);
        configurator.consume(maxBytes);
        configurator.consume(maxRecords);
        return this;
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

    public @Nullable Long scanLimitValue() {
        return convertToLong(scanLimit.getText());
    }

    public @Nullable Long maxBytesValue() {
        return convertToLong(maxBytes.getText());
    }

    public @Nullable Long maxRecordsValue() {
        return convertToLong(maxRecords.getText());
    }

    private @NotNull JComponent configureField(@NotNull ResizableTextField field, @NotNull Icon icon, boolean withLabels) {
        field.setOpaque(false);
        String labelText = field.getToolTipText();
        JBLabel iconLabel = new JBLabel(withLabels ? labelText + ":" : "", icon, SwingConstants.LEFT);
        iconLabel.setToolTipText(labelText);
        iconLabel.setBorder(JBUI.Borders.empty(JBUI.scale(4)));
        iconLabel.setOpaque(false);
        JComponent wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setOpaque(false);
        wrapper.setBorder(JBUI.Borders.empty());
        wrapper.add(iconLabel);
        wrapper.add(field);
        return wrapper;
    }

    private @Nullable Long convertToLong(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}

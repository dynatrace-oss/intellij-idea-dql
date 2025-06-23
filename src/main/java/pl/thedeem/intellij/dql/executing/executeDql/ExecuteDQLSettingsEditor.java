package pl.thedeem.intellij.dql.executing.executeDql;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.components.DynatraceTenantSelector;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.text.NumberFormat;
import java.text.ParseException;

public class ExecuteDQLSettingsEditor extends SettingsEditor<ExecuteDQLRunConfiguration> {
    private final DynatraceTenantSelector<?> tenantSelector;
    private final JPanel myPanel;
    private final TextFieldWithBrowseButton dqlFilePath;
    private final JFormattedTextField defaultScanLimit;
    private final JFormattedTextField maxResultBytes;
    private final JFormattedTextField maxResultRecords;
    private final JBTextField timeframeStart;
    private final JBTextField timeframeEnd;

    public ExecuteDQLSettingsEditor(@NotNull Project project) {
        dqlFilePath = new TextFieldWithBrowseButton();
        dqlFilePath.addBrowseFolderListener(new TextBrowseFolderListener(
                FileChooserDescriptorFactory
                        .singleFile()
                        .withExtensionFilter("dql")
                        .withRoots(ProjectRootManager.getInstance(project).getContentRoots())
        ));
        tenantSelector = new DynatraceTenantSelector<>(new FlowLayout(FlowLayout.LEFT, 0, 0));
        defaultScanLimit = new JFormattedTextField(createNumberFormat());
        maxResultBytes = new JFormattedTextField(createNumberFormat());
        maxResultRecords = new JFormattedTextField(createNumberFormat());
        timeframeStart = new JBTextField();
        timeframeEnd = new JBTextField();
        myPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.dqlFilePath"), dqlFilePath)
                .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.selectedTenant"), tenantSelector)
                .addSeparator()
                .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.defaultScanLimit"), defaultScanLimit)
                .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.maxResultBytes"), maxResultBytes)
                .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.maxResultRecords"), maxResultRecords)
                .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.timeframeStart"), timeframeStart)
                .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.timeframeEnd"), timeframeEnd)
                .getPanel();
    }

    @Override
    protected void resetEditorFrom(ExecuteDQLRunConfiguration runConfig) {
        dqlFilePath.setText(runConfig.getDQLFile());
        tenantSelector.refreshTenantsComboBox();
        String selectedTenantName = runConfig.getOptions().getSelectedTenant();
        tenantSelector.selectTenant(selectedTenantName);

        defaultScanLimit.setValue(runConfig.getOptions().getDefaultScanLimit());
        maxResultBytes.setValue(runConfig.getOptions().getMaxResultBytes());
        maxResultRecords.setValue(runConfig.getOptions().getMaxResultRecords());
        timeframeStart.setText(runConfig.getOptions().getTimeframeStart());
        timeframeEnd.setText(runConfig.getOptions().getTimeframeEnd());
    }

    @Override
    protected void applyEditorTo(@NotNull ExecuteDQLRunConfiguration runConfig) {
        runConfig.setDQLFile(dqlFilePath.getText());
        DynatraceTenant selectedTenant = tenantSelector.getSelectedTenant();

        if (selectedTenant != null) {
            runConfig.getOptions().setSelectedTenant(selectedTenant.getUrl());
        } else {
            runConfig.getOptions().setSelectedTenant(null);
        }

        runConfig.getOptions().setDefaultScanLimit((Long) defaultScanLimit.getValue());
        runConfig.getOptions().setMaxResultBytes((Long) maxResultBytes.getValue());
        runConfig.getOptions().setMaxResultRecords((Long) maxResultRecords.getValue());
        runConfig.getOptions().setTimeframeStart(timeframeStart.getText());
        runConfig.getOptions().setTimeframeEnd(timeframeEnd.getText());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    private NumberFormatter createNumberFormat() {
        NumberFormat format = NumberFormat.getIntegerInstance();
        NumberFormatter formatter = new NumberFormatter(format) {
            @Override
            public Object stringToValue(String text) throws ParseException {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                return super.stringToValue(text);
            }
        };
        formatter.setValueClass(Long.class);
        formatter.setAllowsInvalid(true);
        formatter.setMinimum(null);
        formatter.setMaximum(Long.MAX_VALUE);
        formatter.setCommitsOnValidEdit(true);
        return formatter;
    }
}

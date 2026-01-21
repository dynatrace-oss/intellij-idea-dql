package pl.thedeem.intellij.dql.exec.runConfiguration;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.JBColor;
import com.intellij.ui.RoundedLineBorder;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.components.DynatraceTenantSelector;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;

import javax.swing.*;
import java.awt.*;

public class ExecuteDQLSettingsEditor extends SettingsEditor<ExecuteDQLRunConfiguration> {
    private final static int CODE_EDITOR_HEIGHT = 250;
    private final TextFieldWithBrowseButton dqlFilePath;
    private final EditorTextField dqlQuery;
    private final DynatraceTenantSelector<?> tenantSelector;
    private final JPanel myPanel;
    private final QuerySelectionPanel queryPanel;
    private final DQLQueryExecutionComponent queryParameters;

    public ExecuteDQLSettingsEditor(@NotNull Project project) {
        dqlFilePath = new TextFieldWithBrowseButton();
        dqlQuery = IntelliJUtils.createEditorPanel(project, DynatraceQueryLanguage.INSTANCE, false);
        dqlQuery.setBorder(new RoundedLineBorder(JBColor.namedColor("Editor.Toolbar.borderColor", JBColor.border()), JBUI.scale(8), JBUI.scale(1)));
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        dqlFilePath.addBrowseFolderListener(new TextBrowseFolderListener(
                descriptor
                        .withExtensionFilter("dql")
                        .withRoots(ProjectRootManager.getInstance(project).getContentRoots())
        ));
        tenantSelector = new DynatraceTenantSelector<>(new FlowLayout(FlowLayout.LEFT, 0, 0));
        queryParameters = new DQLQueryExecutionComponent();
        queryPanel = new QuerySelectionPanel(dqlFilePath, dqlQuery);
        myPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.dqlFilePath"), queryPanel)
                .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.selectedTenant"), tenantSelector)
                .addSeparator()
                .addComponent(queryParameters.getPanel())
                .getPanel();

        dqlQuery.setPreferredSize(new Dimension(dqlQuery.getPreferredSize().width, JBUI.scale(CODE_EDITOR_HEIGHT)));
    }

    @Override
    protected void resetEditorFrom(ExecuteDQLRunConfiguration runConfig) {
        tenantSelector.refreshTenantsComboBox();
        String selectedTenantName = runConfig.getOptions().getSelectedTenant();
        tenantSelector.selectTenant(selectedTenantName);

        queryParameters.setDefaultScanLimit(runConfig.getOptions().getDefaultScanLimit());
        queryParameters.setMaxResultBytes(runConfig.getOptions().getMaxResultBytes());
        queryParameters.setMaxResultRecords(runConfig.getOptions().getMaxResultRecords());
        queryParameters.setTimeframeStart(runConfig.getOptions().getTimeframeStart());
        queryParameters.setTimeframeEnd(runConfig.getOptions().getTimeframeEnd());

        dqlFilePath.setText(runConfig.getDQLFile());
        dqlQuery.setText(runConfig.getOptions().getDqlQuery());
        queryPanel.setFromFile(runConfig.getOptions().getFromFileSelected());
    }

    @Override
    protected void applyEditorTo(@NotNull ExecuteDQLRunConfiguration runConfig) {
        ExecuteDQLRunConfigurationOptions options = runConfig.getOptions();
        if (queryPanel.isFromFile()) {
            options.setFromFileSelected(true);
            runConfig.setDQLFile(dqlFilePath.getText());
            options.setDqlQuery(null);
        } else {
            options.setDqlQuery(dqlQuery.getText());
            runConfig.setDQLFile(null);
            options.setFromFileSelected(false);
        }
        DynatraceTenant selectedTenant = tenantSelector.getSelectedTenant();

        if (selectedTenant != null) {
            options.setSelectedTenant(selectedTenant.getName());
        } else {
            options.setSelectedTenant(null);
        }

        options.setDefaultScanLimit(queryParameters.getDefaultScanLimit());
        options.setMaxResultBytes(queryParameters.getMaxResultBytes());
        options.setMaxResultRecords(queryParameters.getMaxResultRecords());
        options.setTimeframeStart(queryParameters.getTimeframeStart());
        options.setTimeframeEnd(queryParameters.getTimeframeEnd());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return myPanel;
    }

    private static final class QuerySelectionPanel extends BorderLayoutPanel {
        private final JRadioButton fromFile;
        private final JRadioButton fromText;
        private final JComponent fileComponent;
        private final JComponent textComponent;

        public QuerySelectionPanel(JComponent fileComponent, JComponent textComponent) {
            super();
            setBorder(JBUI.Borders.empty(10, 0));
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            BorderLayoutPanel selectionPanel = JBUI.Panels.simplePanel();
            ButtonGroup group = new ButtonGroup();
            this.fileComponent = fileComponent;
            this.textComponent = textComponent;
            fromFile = new JRadioButton(DQLBundle.message("runConfiguration.executeDQL.settings.query.fromFile"));
            fromText = new JRadioButton(DQLBundle.message("runConfiguration.executeDQL.settings.query.fromText"));
            fromFile.addActionListener(e -> setFromFile(true));
            fromText.addActionListener(e -> setFromFile(false));
            group.add(fromFile);
            group.add(fromText);
            buttons.add(fromFile);
            buttons.add(fromText);

            selectionPanel.add(fileComponent);
            selectionPanel.add(textComponent, BorderLayout.BEFORE_FIRST_LINE);

            addToTop(buttons);
            addToBottom(selectionPanel);
        }


        public boolean isFromFile() {
            return fromFile.isSelected();
        }

        public void setFromFile(boolean enabled) {
            if (enabled) {
                fileComponent.setVisible(true);
                textComponent.setVisible(false);
                fromFile.setSelected(true);
            } else {
                fileComponent.setVisible(false);
                textComponent.setVisible(true);
                fromText.setSelected(true);
            }
            revalidate();
            repaint();
        }
    }
}

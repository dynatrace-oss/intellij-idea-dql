package pl.thedeem.intellij.dql.executing.executeDql.runConfiguration;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.components.DQLQueryExecutionComponent;
import pl.thedeem.intellij.dql.components.DynatraceTenantSelector;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;

import javax.swing.*;
import java.awt.*;

public class ExecuteDQLSettingsEditor extends SettingsEditor<ExecuteDQLRunConfiguration> {
  private final TextFieldWithBrowseButton dqlFilePath;
  private final DynatraceTenantSelector<?> tenantSelector;
  private final JPanel myPanel;
  private final DQLQueryExecutionComponent queryParameters;

  public ExecuteDQLSettingsEditor(@NotNull Project project) {
    dqlFilePath = new TextFieldWithBrowseButton();
    dqlFilePath.addBrowseFolderListener(new TextBrowseFolderListener(
        FileChooserDescriptorFactory
            .singleFile()
            .withExtensionFilter("dql")
            .withRoots(ProjectRootManager.getInstance(project).getContentRoots())
    ));
    tenantSelector = new DynatraceTenantSelector<>(new FlowLayout(FlowLayout.LEFT, 0, 0));
    queryParameters = new DQLQueryExecutionComponent();
    myPanel = FormBuilder.createFormBuilder()
        .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.dqlFilePath"), dqlFilePath)
        .addLabeledComponent(DQLBundle.message("runConfiguration.executeDQL.settings.selectedTenant"), tenantSelector)
        .addSeparator()
        .addComponent(queryParameters.getPanel())
        .getPanel();
  }

  @Override
  protected void resetEditorFrom(ExecuteDQLRunConfiguration runConfig) {
    dqlFilePath.setText(runConfig.getDQLFile());
    tenantSelector.refreshTenantsComboBox();
    String selectedTenantName = runConfig.getOptions().getSelectedTenant();
    tenantSelector.selectTenant(selectedTenantName);

    queryParameters.setDefaultScanLimit(runConfig.getOptions().getDefaultScanLimit());
    queryParameters.setMaxResultBytes(runConfig.getOptions().getMaxResultBytes());
    queryParameters.setMaxResultRecords(runConfig.getOptions().getMaxResultRecords());
    queryParameters.setTimeframeStart(runConfig.getOptions().getTimeframeStart());
    queryParameters.setTimeframeEnd(runConfig.getOptions().getTimeframeEnd());
  }

  @Override
  protected void applyEditorTo(@NotNull ExecuteDQLRunConfiguration runConfig) {
    runConfig.setDQLFile(dqlFilePath.getText());
    DynatraceTenant selectedTenant = tenantSelector.getSelectedTenant();

    if (selectedTenant != null) {
      runConfig.getOptions().setSelectedTenant(selectedTenant.getName());
    } else {
      runConfig.getOptions().setSelectedTenant(null);
    }

    runConfig.getOptions().setDefaultScanLimit(queryParameters.getDefaultScanLimit());
    runConfig.getOptions().setMaxResultBytes(queryParameters.getMaxResultBytes());
    runConfig.getOptions().setMaxResultRecords(queryParameters.getMaxResultRecords());
    runConfig.getOptions().setTimeframeStart(queryParameters.getTimeframeStart());
    runConfig.getOptions().setTimeframeEnd(queryParameters.getTimeframeEnd());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myPanel;
  }
}

package pl.thedeem.intellij.dql.actions;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.impl.RunDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.executing.runConfiguration.ExecuteDQLConfigurationFactory;
import pl.thedeem.intellij.dql.executing.runConfiguration.ExecuteDQLConfigurationType;
import pl.thedeem.intellij.dql.executing.runConfiguration.ExecuteDQLRunConfiguration;

public class SaveQueryConfigurationAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null || project == null) {
            return;
        }

        RunManager runManager = RunManager.getInstance(project);

        DQLQueryConfigurationService configService = DQLQueryConfigurationService.getInstance(project);
        QueryConfiguration configuration = configService.getQueryConfiguration(file);
        RunnerAndConfigurationSettings config = configuration.getRunConfigName() != null ?
                IntelliJUtils.findConfiguration(configuration.getRunConfigName(), runManager)
                : null;
        if (config == null) {
            config = createNewSettings(
                    DQLBundle.message(
                            "action.DQL.ExecuteScript.runConfigurationName",
                            ActionUtils.generateServiceName(file)
                    ),
                    runManager
            );
        }
        if (config.getConfiguration() instanceof ExecuteDQLRunConfiguration dqlSettings) {
            dqlSettings.loadFromConfiguration(configuration, file);
        }
        if (RunDialog.editConfiguration(project, config, DQLBundle.message("action.DQL.SaveConfigurationAsRunConfig.edit", config.getName()))) {
            runManager.addConfiguration(config);
            runManager.setSelectedConfiguration(config);
        }
    }

    private @NotNull RunnerAndConfigurationSettings createNewSettings(@NotNull String name, @NotNull RunManager runManager) {
        ExecuteDQLConfigurationFactory factory = ExecuteDQLConfigurationType.getInstance().getFactory();
        return runManager.createConfiguration(name, factory);
    }
}

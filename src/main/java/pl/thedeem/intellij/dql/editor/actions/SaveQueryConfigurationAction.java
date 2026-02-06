package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.impl.RunDialog;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;
import pl.thedeem.intellij.dql.exec.runConfiguration.ExecuteDQLConfigurationFactory;
import pl.thedeem.intellij.dql.exec.runConfiguration.ExecuteDQLConfigurationType;
import pl.thedeem.intellij.dql.exec.runConfiguration.ExecuteDQLRunConfiguration;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import java.util.Objects;

public class SaveQueryConfigurationAction extends AnAction {
    public SaveQueryConfigurationAction() {
        super(DQLBundle.message("action.DQL.SaveQueryConfiguration.text"), null, AllIcons.Actions.AddToDictionary);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        RunManager runManager = RunManager.getInstance(project);
        String configName = null;
        QueryConfiguration configuration = null;
        DQLExecutionService service = e.getData(DQLExecutionService.EXECUTION_SERVICE);
        if (service != null) {
            configName = service.getConfiguration().runConfigName();
            configuration = service.getConfiguration();
        } else if (file != null) {
            configName = DQLBundle.message(
                    "action.DQL.ExecuteScript.runConfigurationName",
                    file.getName()
            );
            configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);
        }

        if (configuration == null) {
            return;
        }
        RunnerAndConfigurationSettings config = configuration.getRunConfigName() != null ?
                IntelliJUtils.findConfiguration(configuration.getRunConfigName(), runManager)
                : null;
        if (config == null) {
            config = createNewSettings(configName, runManager);
        }
        if (config.getConfiguration() instanceof ExecuteDQLRunConfiguration dqlSettings) {
            dqlSettings.loadFromConfiguration(configuration, file);
        }
        if (RunDialog.editConfiguration(project, config, DQLBundle.message("action.DQL.SaveQueryConfiguration.edit", config.getName()))) {
            runManager.addConfiguration(config);
            runManager.setSelectedConfiguration(config);
        }
    }

    private @NotNull RunnerAndConfigurationSettings createNewSettings(@Nullable String name, @NotNull RunManager runManager) {
        ExecuteDQLConfigurationFactory factory = ExecuteDQLConfigurationType.getInstance().getFactory();
        return runManager.createConfiguration(Objects.requireNonNullElse(name, ""), factory);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}

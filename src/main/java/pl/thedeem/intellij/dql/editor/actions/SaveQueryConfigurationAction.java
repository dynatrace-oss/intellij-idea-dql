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
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.runConfiguration.ExecuteDQLConfigurationFactory;
import pl.thedeem.intellij.dql.exec.runConfiguration.ExecuteDQLConfigurationType;
import pl.thedeem.intellij.dql.exec.runConfiguration.ExecuteDQLRunConfiguration;
import pl.thedeem.intellij.dql.services.notifications.DQLNotificationsService;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import java.util.List;

public class SaveQueryConfigurationAction extends AnAction {
    public SaveQueryConfigurationAction() {
        super(
                DQLBundle.message("editor.action.saveConfigurationAsRunConfig.title"),
                null,
                AllIcons.Actions.AddToDictionary
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        RunManager runManager = RunManager.getInstance(project);
        QueryConfiguration configuration = e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION);

        if (configuration == null) {
            DQLNotificationsService.getInstance(project).showErrorNotification(
                    DQLBundle.message("editor.action.saveConfigurationAsRunConfig.notifications.title"),
                    DQLBundle.message("editor.action.saveConfigurationAsRunConfig.notifications.missingConfiguration"),
                    project,
                    List.of()
            );
            return;
        }
        RunnerAndConfigurationSettings config = configuration.getRunConfigName() != null ?
                IntelliJUtils.findConfiguration(configuration.getRunConfigName(), runManager)
                : null;
        if (config == null) {
            config = createNewSettings(runManager);
        }
        if (config.getConfiguration() instanceof ExecuteDQLRunConfiguration dqlSettings) {
            dqlSettings.loadFromConfiguration(configuration, file);
        }
        if (RunDialog.editConfiguration(
                project,
                config,
                DQLBundle.message("editor.action.saveConfigurationAsRunConfig.editExisting", config.getName())
        )) {
            runManager.addConfiguration(config);
            runManager.setSelectedConfiguration(config);
        }
    }

    private @NotNull RunnerAndConfigurationSettings createNewSettings(@NotNull RunManager runManager) {
        ExecuteDQLConfigurationFactory factory = ExecuteDQLConfigurationType.getInstance().getFactory();
        return runManager.createConfiguration("", factory);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}

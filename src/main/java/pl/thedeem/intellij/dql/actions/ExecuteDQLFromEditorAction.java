package pl.thedeem.intellij.dql.actions;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.executing.DQLExecutionUtil;
import pl.thedeem.intellij.dql.executing.executeDql.runConfiguration.ExecuteDQLConfigurationFactory;
import pl.thedeem.intellij.dql.executing.executeDql.runConfiguration.ExecuteDQLConfigurationType;
import pl.thedeem.intellij.dql.executing.executeDql.runConfiguration.ExecuteDQLRunConfiguration;

public class ExecuteDQLFromEditorAction extends AnAction {

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        presentation.setEnabledAndVisible(file != null && "dql".equalsIgnoreCase(file.getExtension()));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
       return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(CommonDataKeys.PROJECT);
        @Nullable PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (project == null || psiFile == null || psiFile.getLanguage() != DynatraceQueryLanguage.INSTANCE) {
            return;
        }

        String path = psiFile.getVirtualFile().getPath();
        RunnerAndConfigurationSettings settings = DQLExecutionUtil.findExistingSettings(project, psiFile);
        if (settings == null) {
            settings = createNewSettings(project, psiFile);

            ExecuteDQLRunConfiguration dqlConfig = (ExecuteDQLRunConfiguration) settings.getConfiguration();
            dqlConfig.setDQLFile(DQLExecutionUtil.getProjectRelativePath(path, project));
            DQLExecutionUtil.openRunConfiguration(project);
        } else {
            RunManager.getInstance(project).setSelectedConfiguration(settings);
            ExecuteDQLRunConfiguration dqlConfig = (ExecuteDQLRunConfiguration) settings.getConfiguration();
            dqlConfig.setDQLFile(DQLExecutionUtil.getProjectRelativePath(path, project));
            ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance());
        }
    }

    private @NotNull RunnerAndConfigurationSettings createNewSettings(@NotNull Project project, @NotNull PsiFile psiFile) {
        RunManager runManager = RunManager.getInstance(project);
        ExecuteDQLConfigurationFactory factory = ExecuteDQLConfigurationType.getInstance().getFactory();
        RunnerAndConfigurationSettings newSettings = runManager.createConfiguration(DQLExecutionUtil.getRunConfigName(psiFile), factory);
        runManager.addConfiguration(newSettings);
        runManager.setSelectedConfiguration(newSettings);
        return newSettings;
    }
}

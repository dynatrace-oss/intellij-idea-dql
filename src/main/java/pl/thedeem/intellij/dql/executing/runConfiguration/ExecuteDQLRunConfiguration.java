package pl.thedeem.intellij.dql.executing.runConfiguration;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.executing.DQLExecutionService;
import pl.thedeem.intellij.dql.executing.DQLProcessHandler;
import pl.thedeem.intellij.dql.services.ui.DQLServicesManager;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

public class ExecuteDQLRunConfiguration extends RunConfigurationBase<ExecuteDQLRunConfigurationOptions> {
    protected ExecuteDQLRunConfiguration(Project project,
                                         ConfigurationFactory factory,
                                         String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public ExecuteDQLRunConfigurationOptions getOptions() {
        return (ExecuteDQLRunConfigurationOptions) super.getOptions();
    }

    public String getDQLFile() {
        return getOptions().getDqlPath();
    }

    public void setDQLFile(String dqlFile) {
        getOptions().setDqlPath(dqlFile);
    }


    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new ExecuteDQLSettingsEditor(getProject());
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        FileDocumentManager.getInstance().saveAllDocuments();
        return (exec, programRunner) -> {
            String tenantName = getOptions().getSelectedTenant();
            DynatraceTenant tenant = DynatraceTenantsService.getInstance().findTenant(tenantName);
            QueryConfiguration payload = getConfiguration();
            DQLProcessHandler processHandler = new DQLProcessHandler();
            if (tenant == null) {
                IntelliJUtils.openRunConfiguration(getProject());
                return null;
            }
            DQLExecutionService service = new DQLExecutionService(getName(), getProject(), processHandler);
            ProcessTerminatedListener.attach(processHandler);
            processHandler.startNotify();
            DQLServicesManager.getInstance(getProject()).startExecution(service, payload);
            return new DefaultExecutionResult(null, processHandler, service.getToolbarActions());
        };
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        ExecuteDQLRunConfigurationOptions options = getOptions();
        if (StringUtil.isEmpty(options.getSelectedTenant())) {
            throw new RuntimeConfigurationError(DQLBundle.message("runConfiguration.executeDQL.settings.errors.emptyTenant"));
        }
        String query = getQuery();
        if (StringUtil.isEmpty(query)) {
            throw new RuntimeConfigurationError(DQLBundle.message("runConfiguration.executeDQL.settings.errors.emptyQuery"));
        }
    }

    public @NotNull QueryConfiguration getConfiguration() {
        ExecuteDQLRunConfigurationOptions options = getOptions();
        QueryConfiguration configuration = new QueryConfiguration();
        configuration.setRelatedRunConfiguration(getName());
        configuration.setQuery(getQuery());
        configuration.setTenant(options.getSelectedTenant());
        configuration.setDefaultScanLimit(options.getDefaultScanLimit());
        configuration.setMaxResultBytes(options.getMaxResultBytes());
        configuration.setMaxResultRecords(options.getMaxResultRecords());
        configuration.setTimeframeStart(options.getTimeframeStart());
        configuration.setTimeframeEnd(options.getTimeframeEnd());
        return configuration;
    }

    public void loadFromConfiguration(@NotNull QueryConfiguration configuration, @NotNull PsiFile file) {
        ExecuteDQLRunConfigurationOptions options = getOptions();
        options.setSelectedTenant(configuration.tenant());
        options.setDqlPath(ProjectUtil.calcRelativeToProjectPath(file.getVirtualFile(), getProject(), false, false, false));
        options.setDqlQuery(configuration.query());
        options.setDefaultScanLimit(configuration.defaultScanLimit());
        options.setTimeframeStart(configuration.timeframeStart());
        options.setTimeframeEnd(configuration.timeframeEnd());
        options.setMaxResultBytes(configuration.maxResultBytes());
        options.setMaxResultRecords(configuration.maxResultRecords());
    }

    private @Nullable String getQuery() {
        if (StringUtil.isNotEmpty(getOptions().getDqlQuery())) {
            return getOptions().getDqlQuery();
        }
        return getQueryFromFile(getOptions().getDqlPath(), getProject());
    }

    private @Nullable String getQueryFromFile(@Nullable String dqlFile, @NotNull Project project) {
        if (dqlFile == null) {
            return null;
        }
        PsiFile psiFile = ReadAction.compute(() -> {
            VirtualFile projectFile = ProjectUtil.guessProjectDir(project);
            if (projectFile == null) {
                return null;
            }
            VirtualFile file = projectFile.findFileByRelativePath(dqlFile);
            if (file == null) {
                return null;
            }
            return PsiManager.getInstance(project).findFile(file);
        });
        return psiFile != null ? psiFile.getText() : null;
    }
}

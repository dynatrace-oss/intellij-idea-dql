package pl.thedeem.intellij.dql.executing.executeDql.runConfiguration;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.executing.DQLExecutionUtil;
import pl.thedeem.intellij.dql.executing.DQLParsedQuery;
import pl.thedeem.intellij.dql.executing.executeDql.DQLExecutionService;
import pl.thedeem.intellij.dql.executing.services.DQLServicesManager;
import pl.thedeem.intellij.dql.sdk.model.DQLExecutePayload;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.nio.file.Path;

public class ExecuteDQLRunConfiguration extends RunConfigurationBase<ExecuteDQLRunConfigurationOptions> {
    protected ExecuteDQLRunConfiguration(Project project,
                                         ConfigurationFactory factory,
                                         String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    protected ExecuteDQLRunConfigurationOptions getOptions() {
        return (ExecuteDQLRunConfigurationOptions) super.getOptions();
    }

    public String getDQLFile() {
        return getOptions().getDqlPath();
    }

    public void setDQLFile(String dqlFile) {
        getOptions().setDqlPath(dqlFile);
    }

    public String getTenantName() {
        return getOptions().getSelectedTenant();
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
            DQLExecutePayload payload = getPayload(getOptions());
            DQLProcessHandler processHandler = new DQLProcessHandler();
            if (payload == null || tenant == null || StringUtil.isEmpty(payload.getQuery())) {
                DQLExecutionUtil.openRunConfiguration(getProject());
                return null;
            }
            DQLExecutionService service = new DQLExecutionService(processHandler, getName(), getProject(), tenant, payload);
            ProcessTerminatedListener.attach(processHandler);
            processHandler.startNotify();
            DQLServicesManager.getInstance(getProject()).startExecution(service);
            return new DefaultExecutionResult(null, processHandler, service.getToolbarActions());
        };
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (StringUtil.isEmpty(getDQLFile())) {
            throw new RuntimeConfigurationError(DQLBundle.message("runConfiguration.executeDQL.settings.errors.emptyFile"));
        }
        if (StringUtil.isEmpty(getOptions().getSelectedTenant())) {
            throw new RuntimeConfigurationError(DQLBundle.message("runConfiguration.executeDQL.settings.errors.emptyTenant"));
        }
    }

    protected DQLExecutePayload getPayload(ExecuteDQLRunConfigurationOptions options) {
        String dqlContent = "";
        PsiFile dqlFile = DQLUtil.openFile(getProject(), Path.of(DQLExecutionUtil.getProjectAbsolutePath(options.getDqlPath(), getProject())).toString());
        if (dqlFile != null) {
            DQLParsedQuery parsedQuery = new DQLParsedQuery(dqlFile);
            dqlContent = parsedQuery.getParsedQuery();
        }
        DQLExecutePayload payload = new DQLExecutePayload(dqlContent);
        payload.setDefaultScanLimitGbytes(options.getDefaultScanLimit());
        payload.setMaxResultBytes(options.getMaxResultBytes());
        payload.setMaxResultRecords(options.getMaxResultRecords());
        payload.setDefaultTimeframeStart(options.getTimeframeStart());
        payload.setDefaultTimeframeEnd(options.getTimeframeEnd());
        return payload;
    }
}

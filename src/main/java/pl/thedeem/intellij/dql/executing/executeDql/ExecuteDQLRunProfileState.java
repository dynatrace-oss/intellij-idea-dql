package pl.thedeem.intellij.dql.executing.executeDql;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.executing.DQLExecutionUtil;
import pl.thedeem.intellij.dql.executing.services.DQLServicesManager;
import pl.thedeem.intellij.dql.sdk.model.DQLExecutePayload;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

public class ExecuteDQLRunProfileState implements RunProfileState {

    private final Project project;
    private final ExecuteDQLRunConfiguration configuration;

    public ExecuteDQLRunProfileState(@NotNull ExecuteDQLRunConfiguration configuration, @NotNull Project project) {
        this.configuration = configuration;
        this.project = project;
    }

    public DQLExecutePayload getPayload(ExecuteDQLRunConfigurationOptions options) throws IOException {
        String dqlContent = Files.readString(Path.of(DQLExecutionUtil.getProjectAbsolutePath(options.getDqlPath(), project)));
        DQLExecutePayload payload = new DQLExecutePayload(dqlContent);
        payload.setDefaultScanLimitGbytes(options.getDefaultScanLimit());
        payload.setMaxResultBytes(options.getMaxResultBytes());
        payload.setMaxResultRecords(options.getMaxResultRecords());
        payload.setDefaultTimeframeStart(options.getTimeframeStart());
        payload.setDefaultTimeframeEnd(options.getTimeframeEnd());
        return payload;
    }

    @Nullable
    @Override
    public ExecutionResult execute(Executor executor, @NotNull ProgramRunner<?> runner) {
        String tenantName = configuration.getOptions().getSelectedTenant();
        DynatraceTenant tenant = DynatraceTenantsService.getInstance().getTenantByUrl(tenantName);
        DQLExecutePayload payload;

        try {
            payload = getPayload(configuration.getOptions());
        } catch (IOException e) {
            DQLExecutionUtil.openRunConfiguration(project);
            return null;
        }
        if (tenant == null || StringUtil.isEmpty(payload.getQuery())) {
            DQLExecutionUtil.openRunConfiguration(project);
            return null;
        }

        ConsoleView consoleView = new ConsoleViewImpl(project, false);
        DQLExecutionService service = new DQLExecutionService(configuration.getName(), project, tenant, payload);
        final AtomicReference<ScheduledFuture<?>> futureReference = new AtomicReference<>();
        ProcessHandler processHandler = createHandler(futureReference);
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            DQLServicesManager.getInstance(project).startExecution(service);
            service.execute(futureReference);
            processHandler.startNotify();
        });

        return new DefaultExecutionResult(consoleView, processHandler, service.getToolbarActions());
    }

    private ProcessHandler createHandler(AtomicReference<ScheduledFuture<?>> futureReference) {
        return new ProcessHandler() {
            @Override
            protected void destroyProcessImpl() {
                ScheduledFuture<?> pollingFuture = futureReference.get();
                if (pollingFuture != null) {
                    pollingFuture.cancel(true);
                }
                notifyProcessTerminated(0);
            }

            @Override
            protected void detachProcessImpl() {
                notifyProcessDetached();
            }

            @Override
            public boolean detachIsDefault() {
                return false;
            }

            @Override
            public OutputStream getProcessInput() {
                return null;
            }

            @Override
            public boolean isProcessTerminated() {
                return futureReference.get() == null || futureReference.get().isCancelled();
            }

            @Override
            public boolean isProcessTerminating() {
                return isProcessTerminated();
            }
        };
    }
}

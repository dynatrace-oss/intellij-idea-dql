package pl.thedeem.intellij.dql.exec;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.internal.StringUtil;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.sdk.DynatraceRestClient;
import pl.thedeem.intellij.common.sdk.model.DQLExecutePayload;
import pl.thedeem.intellij.common.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.common.services.ManagedService;
import pl.thedeem.intellij.common.services.ManagedServiceGroup;
import pl.thedeem.intellij.common.services.ProjectServicesManager;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.editor.actions.QueryConfigurationAction;
import pl.thedeem.intellij.dql.exec.panel.DQLExecutionErrorPanel;
import pl.thedeem.intellij.dql.exec.panel.DQLExecutionResult;
import pl.thedeem.intellij.dql.fileProviders.DQLQueryConsoleVirtualFile;
import pl.thedeem.intellij.dql.fileProviders.DQLResultVirtualFile;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.query.DQLQueryParserService;
import pl.thedeem.intellij.dql.services.ui.ConnectedTenantsServiceGroup;
import pl.thedeem.intellij.dql.services.ui.TenantServiceGroup;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import javax.swing.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DQLExecutionService implements ManagedService, UiDataProvider {
    public static final DataKey<DQLExecutionService> EXECUTION_SERVICE = DataKey.create("executionService");

    private static final Logger logger = Logger.getInstance(DQLExecutionService.class);
    private final static int POLL_INTERVAL = 200;

    private final DQLProcessHandler processHandler;
    private final Project project;
    private final String name;
    private final AtomicReference<ScheduledFuture<?>> pollingFutureRef = new AtomicReference<>(null);
    private final AtomicReference<Boolean> stopping = new AtomicReference<>(false);
    private final AtomicReference<Boolean> loading = new AtomicReference<>(false);
    private final DQLExecutionResult resultPanel;
    private final QueryConfiguration configuration;
    private final QueryConfiguration configurationCopy;

    private String requestToken;
    private DQLPollResponse result;
    private String executionId;
    private DefaultActionGroup actions;

    public DQLExecutionService(@NotNull String name, @NotNull QueryConfiguration params, @NotNull Project project, @NotNull DQLProcessHandler processHandler) {
        this.project = project;
        this.name = name;
        this.processHandler = processHandler;
        this.configuration = params;
        this.configurationCopy = params.copy();
        this.resultPanel = new DQLExecutionResult(project);
        this.resultPanel.setQueryConfiguration(params);
    }

    @Override
    public void dispose() {
        stopExecution();
    }

    @Override
    public @NotNull String getServiceId() {
        return name;
    }

    @Override
    public @NotNull List<ManagedServiceGroup> getParentGroups() {
        String tenant = configuration.tenant();
        if (tenant == null) {
            return List.of(ConnectedTenantsServiceGroup.getInstance());
        }
        return List.of(ConnectedTenantsServiceGroup.getInstance(), new TenantServiceGroup(tenant));
    }

    public void startExecution() {
        this.resultPanel.setExecutionTime(Instant.now().atZone(ZoneId.systemDefault()));
        if (configuration.query() == null) {
            resultPanel.show(new DQLExecutionErrorPanel(DQLBundle.message("services.executeDQL.errors.invalidPayload")));
            return;
        }
        DynatraceTenant tenant = DynatraceTenantsService.getInstance().findTenant(configuration.tenant());
        if (tenant == null) {
            resultPanel.show(new DQLExecutionErrorPanel(DQLBundle.message("services.executeDQL.errors.missingTenant", configuration.tenant())));
            return;
        }
        String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
        DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());
        DQLExecutePayload payload = this.preparePayload(configuration, project);
        this.executionId = String.valueOf(payload.getQuery().hashCode());
        logger.info(String.format("Executing a DQL query with hash %s on the tenant: %s", payload.getQuery().hashCode(), tenant.getName()));
        this.loading.set(true);
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            requestToken = this.executeApiCall(() -> {
                DQLExecuteResponse executedResponse = client.executeDQL(payload, apiToken);
                logger.info(String.format(
                        "The DQL query %s for the tenant %s was started to be executed and has a request token: %s (state: %s)",
                        this.executionId,
                        tenant.getName(),
                        executedResponse.getRequestToken(),
                        executedResponse.getState()
                ));
                String token = executedResponse.getRequestToken();
                resultPanel.update(executedResponse);
                return token;
            });

            if (requestToken != null) {
                pollingFutureRef.set(startPollingRequest(requestToken, client, apiToken));
            }
        });
    }

    public void stopExecution() {
        DynatraceTenant tenant = DynatraceTenantsService.getInstance().findTenant(configuration != null ? configuration.tenant() : null);
        if (tenant == null || executionId == null) {
            return;
        }
        stopping.set(true);
        resultPanel.show(new InformationComponent(
                DQLBundle.message("services.executeDQL.cancelRequested", this.requestToken),
                AllIcons.General.Information)
        );
        ScheduledFuture<?> scheduledFuture = pollingFutureRef.get();
        if (scheduledFuture != null && !scheduledFuture.isCancelled() && !scheduledFuture.isDone() && this.requestToken != null) {
            scheduledFuture.cancel(true);
            String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
            DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());
            logger.info(String.format(
                    "Stopping the query execution %s with the token %s for the DQL query %s (tenant %s)",
                    executionId,
                    requestToken,
                    executionId,
                    tenant.getName()
            ));
            this.executeApiCall(() -> {
                DQLPollResponse result = client.cancelDQL(requestToken, apiToken);
                logger.info(String.format(
                        "The query %s with ID %s was stopped on tenant %s. Returned: %s",
                        executionId,
                        requestToken,
                        tenant.getName(),
                        result != null
                ));
                processHandler.notifyExecutionFinished();
                return result;
            });
        }
        stopping.set(false);
    }

    public boolean isRunning() {
        if (stopping.get() || loading.get()) {
            return true;
        }
        ScheduledFuture<?> scheduledFuture = this.pollingFutureRef.get();
        return scheduledFuture != null && !scheduledFuture.isCancelled() && !scheduledFuture.isDone();
    }

    @Override
    public @Nullable JComponent getContentComponent() {
        return this.resultPanel;
    }

    @Override
    public @Nullable ActionGroup getToolbarActions() {
        if (actions == null) {
            actions = new DefaultActionGroup();
            actions.setInjectedContext(true);
            actions.addAction(new QueryConfigurationAction());
            actions.addSeparator();
            actions.addAction(resultPanel.getToolbarActions());
            actions.addSeparator();
            actions.addAction(new AnAction(
                    DQLBundle.message("services.executeDQL.actions.openInNewTab.title"),
                    null,
                    AllIcons.Actions.MoveTo2
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                    DQLPollResponse result = getResult();
                    if (result == null) {
                        return;
                    }
                    FileEditorManager.getInstance(project)
                            .openFile(new DQLResultVirtualFile(
                                    DQLBundle.message(
                                            "services.executeDQL.actions.openInNewTab.tabTitle",
                                            getPresentation().getPresentableText()
                                    ),
                                    result
                            ), true);
                }

                @Override
                public void update(@NotNull AnActionEvent e) {
                    e.getPresentation().setEnabledAndVisible(getResult() != null);
                }

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }
            });
            actions.addAction(new AnAction(
                    DQLBundle.message("services.executeDQL.actions.close.title"),
                    null,
                    AllIcons.General.Close
            ) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                    stopExecution();
                    ProjectServicesManager.getInstance(project).unregisterService(DQLExecutionService.this);
                }

                @Override
                public @NotNull ActionUpdateThread getActionUpdateThread() {
                    return ActionUpdateThread.EDT;
                }
            });
        }
        return actions;
    }

    @Override
    public @NotNull ItemPresentation getPresentation() {
        return new StandardItemPresentation(this.name, null, DQLIcon.DYNATRACE_LOGO);
    }

    @Override
    public void uiDataSnapshot(@NotNull DataSink dataSink) {
        dataSink.set(EXECUTION_SERVICE, this);
        dataSink.set(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION, configurationCopy);
        dataSink.set(QueryConfigurationAction.SHOW_TIMEFRAME, false);
        dataSink.set(QueryConfigurationAction.SHOW_TENANT_SELECTION, false);
        dataSink.set(QueryConfigurationAction.SHOW_CONFIGURATION, false);
    }

    public @Nullable DQLPollResponse getResult() {
        return result != null ? result : null;
    }

    public @NotNull QueryConfiguration getConfiguration() {
        return configuration;
    }

    private ScheduledFuture<?> startPollingRequest(@NotNull String requestToken, @NotNull DynatraceRestClient client, @Nullable String apiToken) {
        return AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
                () -> {
                    logger.info(String.format("Verifying if the DQL query %s with the request token %s has finished...", executionId, requestToken));
                    this.executeApiCall(() -> {
                        result = client.pollDQLState(requestToken, apiToken);
                        logger.info(String.format(
                                "Polling response for the DQL query %s with the request token %s: state: %s, progress: %s",
                                executionId,
                                requestToken,
                                result.state,
                                result.progress
                        ));

                        if (result.isFinished()) {
                            logger.info(String.format("The DQL query %s - %s was executed correctly.", executionId, requestToken));
                            pollingFutureRef.get().cancel(false);
                            processHandler.notifyExecutionFinished();
                            this.loading.set(false);
                        }
                        resultPanel.update(result);
                        return result;
                    });
                },
                200,
                POLL_INTERVAL,
                TimeUnit.MILLISECONDS);
    }

    private <T> T executeApiCall(@NotNull Callable<T> apiCall) {
        try {
            T result = apiCall.call();
            ActivityTracker.getInstance().inc();
            return result;
        } catch (Exception e) {
            logger.warn("An error was found when executing the API call to the DT tenant. Cancelling the execution...", e);
            processHandler.notifyExecutionError(e.getMessage());
            if (pollingFutureRef.get() != null) {
                pollingFutureRef.get().cancel(true);
            }
            resultPanel.update(e);
        } finally {
            loading.set(false);
            stopping.set(false);
        }
        return null;
    }

    private @NotNull DQLExecutePayload preparePayload(@NotNull QueryConfiguration configuration, @NotNull Project project) {
        DQLQueryParserService parser = DQLQueryParserService.getInstance();
        DQLQueryParserService.ParseResult parsed = WriteCommandAction.runWriteCommandAction(
                project,
                (Computable<DQLQueryParserService.ParseResult>) () -> parser.getSubstitutedQuery(configuration.query(), project, configuration.definedVariables()));
        DQLExecutePayload result = new DQLExecutePayload(parsed.parsed());
        if (!StringUtil.isBlank(configuration.timeframeStart())) {
            try {
                result.setDefaultTimeframeStart(DQLUtil.parseUserTime(configuration.timeframeStart()));
            } catch (IllegalArgumentException ignored) {
                result.setDefaultTimeframeStart(null);
            }
        }
        if (!StringUtil.isBlank(configuration.timeframeEnd())) {
            try {
                result.setDefaultTimeframeEnd(DQLUtil.parseUserTime(configuration.timeframeEnd()));
            } catch (IllegalArgumentException ignored) {
                result.setDefaultTimeframeEnd(null);
            }
        }
        if (!StringUtil.isBlank(result.getDefaultTimeframeStart()) && StringUtil.isBlank(result.getDefaultTimeframeEnd())) {
            result.setDefaultTimeframeEnd(DQLUtil.getCurrentTimeTimestamp());
        }
        result.setDefaultScanLimitGbytes(configuration.defaultScanLimit());
        result.setMaxResultBytes(configuration.maxResultBytes());
        result.setMaxResultRecords(configuration.maxResultRecords());
        result.setTimezone(ZoneId.systemDefault().getId());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DQLExecutionService that = (DQLExecutionService) o;
        return Objects.equals(configuration.tenant(), that.configuration.tenant())
                && Objects.equals(getServiceId(), that.getServiceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                configuration.tenant(),
                getServiceId()
        );
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        String originalFile = configuration.originalFile();
        VirtualFile virtualFile = null;
        if (originalFile != null) {
            virtualFile = IntelliJUtils.getProjectRelativeFile(originalFile, project);
        }
        if (virtualFile == null) {
            virtualFile = new DQLQueryConsoleVirtualFile(this.name, this.configuration.query())
                    .setInitialConfiguration(this.configuration);
        }
        return new OpenFileDescriptor(project, virtualFile);
    }
}

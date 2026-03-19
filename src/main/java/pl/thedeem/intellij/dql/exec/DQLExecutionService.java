package pl.thedeem.intellij.dql.exec;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.Disposable;
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
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.internal.StringUtil;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.common.StandardItemPresentation;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.sdk.model.DQLExecutePayload;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.common.services.ManagedService;
import pl.thedeem.intellij.common.services.ManagedServiceGroup;
import pl.thedeem.intellij.common.services.ProjectServicesManager;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.editor.actions.QueryConfigurationAction;
import pl.thedeem.intellij.dql.exec.panel.DQLExecuteInProgressPanel;
import pl.thedeem.intellij.dql.exec.panel.DQLExecutionErrorPanel;
import pl.thedeem.intellij.dql.exec.panel.DQLExecutionResult;
import pl.thedeem.intellij.dql.fileProviders.DQLQueryConsoleVirtualFile;
import pl.thedeem.intellij.dql.fileProviders.DQLResultVirtualFile;
import pl.thedeem.intellij.dql.services.dynatrace.DynatraceRestService;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.query.DQLQueryParserService;
import pl.thedeem.intellij.dql.services.ui.ConnectedTenantsServiceGroup;
import pl.thedeem.intellij.dql.services.ui.TenantServiceGroup;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class DQLExecutionService implements ManagedService, UiDataProvider {
    public static final DataKey<DQLExecutionService> EXECUTION_SERVICE = DataKey.create("executionService");
    private static final Logger logger = Logger.getInstance(DQLExecutionService.class);

    private final DQLProcessHandler processHandler;
    private final Project project;
    private final String name;
    private final DefaultActionGroup additionalActions = new DefaultActionGroup();
    private final AtomicReference<CompletableFuture<DQLPollResponse>> executionFuture = new AtomicReference<>(null);
    private final BorderLayoutPanel contentPanel;
    private final QueryConfiguration configuration;
    private final QueryConfiguration configurationCopy;

    private String executionId;
    private DefaultActionGroup actions;
    private ZonedDateTime executionTime;

    public DQLExecutionService(@NotNull String name, @NotNull QueryConfiguration params, @NotNull Project project, @NotNull DQLProcessHandler processHandler) {
        this.project = project;
        this.name = name;
        this.processHandler = processHandler;
        this.configuration = params;
        this.configurationCopy = params.copy();
        this.contentPanel = new BorderLayoutPanel();
        this.contentPanel.addToCenter(new InformationComponent(DQLBundle.message("services.executeDQL.information.startedExecuting"), AllIcons.General.Information));
    }

    @Override
    public void dispose() {
        ApplicationManager.getApplication().invokeLater(this::disposeAndClearContent);
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

    @Override
    public @Nullable JComponent getContentComponent() {
        return this.contentPanel;
    }

    @Override
    public @Nullable ActionGroup getToolbarActions() {
        if (actions == null) {
            actions = new DefaultActionGroup();
            actions.setInjectedContext(true);
            actions.addAction(new QueryConfigurationAction());
            actions.addSeparator();
            actions.addAction(additionalActions);
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
                    e.getPresentation().setEnabledAndVisible(!isRunning() && getResult() != null);
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
        dataSink.set(QueryConfigurationAction.SHOW_QUERY_VALIDATION_OPTION, false);
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

    public void startExecution() {
        executionTime = Instant.now().atZone(ZoneId.systemDefault());
        disposeAndClearContent();
        additionalActions.removeAll();

        if (configuration.query() == null) {
            contentPanel.addToCenter(new DQLExecutionErrorPanel(
                    DQLBundle.message("services.executeDQL.errors.invalidPayload"),
                    configuration.query(),
                    project
            ));
            return;
        }

        DynatraceTenant tenant = DynatraceTenantsService.getInstance().findTenant(configuration.tenant());
        if (tenant == null) {
            contentPanel.addToCenter(new DQLExecutionErrorPanel(
                    DQLBundle.message("services.executeDQL.errors.missingTenant", configuration.tenant()),
                    configuration.query(),
                    project
            ));
            return;
        }

        DQLExecuteInProgressPanel progressPanel = new DQLExecuteInProgressPanel();
        contentPanel.addToCenter(progressPanel);

        DQLExecutePayload payload = preparePayload(configuration, project);
        executionId = String.valueOf(payload.getQuery().hashCode());
        logger.info(String.format("Executing a DQL query with hash %s on the tenant: %s", executionId, tenant.getName()));

        CompletableFuture<DQLPollResponse> future = DynatraceRestService.getInstance(project).executeQuery(tenant, payload, progressPanel::update);
        executionFuture.set(future);

        future.whenComplete((result, error) -> {
            ActivityTracker.getInstance().inc();
            if (future.isCancelled() || error instanceof CancellationException) {
                processHandler.notifyExecutionFinished();
                return;
            }
            if (error != null) {
                logger.warn("An error occurred while executing DQL query " + executionId, error);
                processHandler.notifyExecutionError(error.getMessage());
                Exception exception = error instanceof Exception e ? e : new RuntimeException(error);
                ApplicationManager.getApplication().invokeLater(() -> {
                    disposeAndClearContent();
                    contentPanel.add(new DQLExecutionErrorPanel(exception, configuration.query(), project));
                    contentPanel.revalidate();
                    contentPanel.repaint();
                });
                return;
            }
            logger.info(String.format("The DQL query %s was executed correctly.", executionId));
            processHandler.notifyExecutionFinished();
            ApplicationManager.getApplication().invokeLater(() -> {
                disposeAndClearContent();
                if (result.getResult() != null) {
                    DQLExecutionResult resultPanel = new DQLExecutionResult(project, result.getResult(), executionTime, configuration);
                    contentPanel.add(resultPanel);
                    additionalActions.addAction(resultPanel.getToolbarActions());
                } else {
                    contentPanel.add(new InformationComponent(
                            DQLBundle.message("services.executeDQL.information.missingResults"),
                            AllIcons.General.Information
                    ));
                }
                contentPanel.revalidate();
                contentPanel.repaint();
            });
        });
    }

    public void stopExecution() {
        CompletableFuture<DQLPollResponse> future = executionFuture.get();
        if (future == null || future.isDone()) {
            return;
        }
        String cancelMessage = DQLBundle.message("services.executeDQL.cancelRequested");
        ApplicationManager.getApplication().invokeLater(() -> {
            disposeAndClearContent();
            contentPanel.addToCenter(new InformationComponent(cancelMessage, AllIcons.General.Information));
        });
        future.cancel(true);
    }

    public boolean isRunning() {
        CompletableFuture<DQLPollResponse> future = executionFuture.get();
        return future != null && !future.isDone();
    }

    public @NotNull QueryConfiguration getConfiguration() {
        return configuration;
    }

    private @Nullable DQLPollResponse getResult() {
        CompletableFuture<DQLPollResponse> future = executionFuture.get();
        if (future == null || !future.isDone() || future.isCancelled() || future.isCompletedExceptionally()) {
            return null;
        }
        return future.getNow(null);
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

    private void disposeAndClearContent() {
        for (Component component : contentPanel.getComponents()) {
            if (component instanceof Disposable disposable) {
                disposable.dispose();
            }
        }
        contentPanel.removeAll();
    }
}

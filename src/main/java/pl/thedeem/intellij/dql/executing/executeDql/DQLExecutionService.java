package pl.thedeem.intellij.dql.executing.executeDql;

import com.intellij.icons.AllIcons;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.components.actions.OpenQueryMetadataAction;
import pl.thedeem.intellij.dql.components.actions.SaveAsFileAction;
import pl.thedeem.intellij.dql.executing.services.DQLServicesManager;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.sdk.DynatraceRestClient;
import pl.thedeem.intellij.dql.sdk.errors.DQLErrorResponseException;
import pl.thedeem.intellij.dql.sdk.errors.DQLNotAuthorizedException;
import pl.thedeem.intellij.dql.sdk.model.DQLExecutePayload;
import pl.thedeem.intellij.dql.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.dql.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.sdk.model.errors.DQLErrorResponse;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DQLExecutionService {
    private final static int POLL_INTERVAL = 1000;

    private final Project project;
    private final DynatraceTenant tenant;
    private final DQLExecutePayload payload;
    private final DQLResultPanel resultPanel;
    private final String name;
    private final ActionGroup actionGroup;
    private DQLPollResponse result;

    public DQLExecutionService(@NotNull String name, @NotNull Project project, @NotNull DynatraceTenant tenant, @NotNull DQLExecutePayload payload) {
        this.project = project;
        this.tenant = tenant;
        this.payload = payload;
        this.resultPanel = new DQLResultPanel();
        this.name = name;

        actionGroup = createActionsGroup();
    }

    public void execute(final AtomicReference<ScheduledFuture<?>> pollingFutureRef) {
        String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
        DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());

        ProgressManager.getInstance().run(new Task.Backgroundable(project, name) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    if (myProject == null) {
                        return;
                    }
                    indicator.setFraction(0.0);
                    indicator.setText(DQLBundle.message("runConfiguration.executeDQL.indicator.starting", name));
                    DQLExecuteResponse executedResponse = client.executeDQL(payload, apiToken);
                    resultPanel.registerProgress(executedResponse);
                    indicator.setText(DQLBundle.message("runConfiguration.executeDQL.indicator.awaiting", name));
                    pollingFutureRef.set(AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(() -> {
                        try {
                            if (indicator.isCanceled()) {
                                pollingFutureRef.get().cancel(true);
                                return;
                            }
                            result = client.pollDQLState(executedResponse.getRequestToken(), apiToken);
                            resultPanel.registerProgress(result);
                            indicator.setFraction(result.progress / 100.0);
                            if (result.isFinished()) {
                                pollingFutureRef.get().cancel(false);
                            }
                        } catch (IOException | InterruptedException | DQLNotAuthorizedException |
                                 DQLErrorResponseException e) {
                            stopExecution(indicator, null, e);
                        }
                    }, 200, POLL_INTERVAL, TimeUnit.MILLISECONDS));
                } catch (InterruptedException | IOException e) {
                    stopExecution(indicator, null, e);
                } catch (DQLNotAuthorizedException e) {
                    stopExecution(indicator, e.getResponse(), e);
                } catch (DQLErrorResponseException e) {
                    stopExecution(indicator, e.getResponse(), e);
                }
            }

            private void stopExecution(ProgressIndicator indicator, DQLErrorResponse<?> errorResponse, Exception exception) {
                indicator.setFraction(1.0);
                indicator.setText(DQLBundle.message("runConfiguration.executeDQL.indicator.cancelled", name));
                if (pollingFutureRef.get() != null) {
                    pollingFutureRef.get().cancel(true);
                }
                resultPanel.showError(errorResponse, exception);
            }
        });
    }

    public @Nullable DQLResult getResult() {
        return result != null ? result.getResult() : null;
    }

    public @Nullable JComponent getContentComponent() {
        return this.resultPanel;
    }

    public @Nullable ActionGroup getToolbarActions() {
        return actionGroup;
    }

    public @NotNull ItemPresentation getPresentation() {
        return new DQLItemPresentation(this.name, null, DQLIcon.DYNATRACE_LOGO);
    }

    public ActionGroup createActionsGroup() {
        DQLExecutionService service = this;
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new AnAction(DQLBundle.message("components.tableResults.actions.closeExecution"), null, AllIcons.Actions.Close) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                DQLServicesManager.getInstance(project).stopExecution(service);
            }
        });
        group.add(new AnAction(DQLBundle.message("components.tableResults.actions.refreshExecution"), null, AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
                service.execute(new AtomicReference<>());
            }
        });
        group.addSeparator();
        group.add(new SaveAsFileAction(DQLBundle.message("components.tableResults.actions.exportAsJson"), null, service));
        group.add(new OpenQueryMetadataAction(DQLBundle.message("components.tableResults.actions.showMetadata"), null, service));
        return group;
    }
}

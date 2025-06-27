package pl.thedeem.intellij.dql.executing.executeDql;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.components.DQLResultPanel;
import pl.thedeem.intellij.dql.components.actions.OpenQueryMetadataAction;
import pl.thedeem.intellij.dql.components.actions.SaveAsFileAction;
import pl.thedeem.intellij.dql.executing.executeDql.runConfiguration.DQLProcessHandler;
import pl.thedeem.intellij.dql.executing.services.DQLServicesManager;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.sdk.DynatraceRestClient;
import pl.thedeem.intellij.dql.sdk.errors.DQLErrorResponseException;
import pl.thedeem.intellij.dql.sdk.errors.DQLNotAuthorizedException;
import pl.thedeem.intellij.dql.sdk.model.DQLExecutePayload;
import pl.thedeem.intellij.dql.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.dql.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.intellij.openapi.diagnostic.Logger;

public class DQLExecutionService {
   private static final Logger logger = Logger.getInstance(DQLExecutionService.class);
   private final static int POLL_INTERVAL = 200;

   private final DQLProcessHandler processHandler;
   private final Project project;
   private final DynatraceTenant tenant;
   private final DQLExecutePayload payload;
   private final DQLResultPanel resultPanel;
   private final String name;
   private final ActionGroup actionGroup;
   private DQLPollResponse result;
   private final AtomicReference<ScheduledFuture<?>> pollingFutureRef = new AtomicReference<>(null);
   private final AtomicReference<Boolean> stopping = new AtomicReference<>(false);
   private final AtomicReference<Boolean> loading = new AtomicReference<>(false);
   private String requestToken;

   public DQLExecutionService(
       @NotNull DQLProcessHandler processHandler,
       @NotNull String name,
       @NotNull Project project,
       @NotNull DynatraceTenant tenant,
       @NotNull DQLExecutePayload payload
   ) {
      this.processHandler = processHandler;
      this.project = project;
      this.tenant = tenant;
      this.payload = payload;
      this.resultPanel = new DQLResultPanel();
      this.name = name;
      this.actionGroup = createActionsGroup();
   }

   public void execute() {
      String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
      DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());

      logger.info(String.format("Executing a DQL query with hash %s on the tenant: %s", payload.getQuery().hashCode(), tenant.getName()));
      this.executeApiCall(() -> {
         this.loading.set(true);
         DQLExecuteResponse executedResponse = client.executeDQL(payload, apiToken);
         logger.info(String.format(
             "The DQL query %s for the tenant %s was started to be executed and has a request token: %s (state: %s)",
             payload.getQuery().hashCode(),
             tenant.getName(),
             executedResponse.getRequestToken(),
             executedResponse.getState()
         ));

         requestToken = executedResponse.getRequestToken();
         resultPanel.registerProgress(executedResponse);

         pollingFutureRef.set(AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay(
             () -> {
                logger.info(String.format(
                    "Verifying if the DQL query %s for the tenant %s with the request token %s has finished...",
                    payload.getQuery().hashCode(),
                    tenant.getName(),
                    executedResponse.getRequestToken()
                ));
                this.executeApiCall(() -> {
                   result = client.pollDQLState(executedResponse.getRequestToken(), apiToken);
                   resultPanel.registerProgress(result);

                   logger.info(String.format(
                       "Polling response for the DQL query %s for the tenant %s with the request token %s: state: %s, progress: %s",
                       payload.getQuery().hashCode(),
                       tenant.getName(),
                       executedResponse.getRequestToken(),
                       result.state,
                       result.progress
                   ));

                   if (result.isFinished()) {
                      logger.info(String.format(
                          "The DQL query %s - %s on the tenant %s was executed correctly.",
                          payload.getQuery().hashCode(),
                          requestToken,
                          tenant.getName()
                      ));
                      pollingFutureRef.get().cancel(false);
                      processHandler.notifyExecutionFinished();
                      this.loading.set(false);
                   }
                });
             },
             200,
             POLL_INTERVAL,
             TimeUnit.MILLISECONDS)
         );
      });
   }

   public void stopExecution() {
      stopping.set(true);
      ScheduledFuture<?> scheduledFuture = pollingFutureRef.get();
      if (scheduledFuture != null && !scheduledFuture.isCancelled() && !scheduledFuture.isDone() && this.requestToken != null) {
         scheduledFuture.cancel(true);
         String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
         DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());
         logger.info(String.format(
             "Stopping the query execution %s with the token %s for the DQL query %s (tenant %s)",
             payload.getQuery().hashCode(),
             requestToken,
             payload.getQuery().hashCode(),
             tenant.getName()
         ));
         this.executeApiCall(() -> {
            DQLPollResponse result = client.cancelDQL(requestToken, apiToken);
            logger.info(String.format(
                "The query %s with ID %s was stopped on tenant %s. Returned: %s",
                payload.getQuery().hashCode(),
                requestToken,
                tenant.getName(),
                result != null
            ));
            processHandler.notifyExecutionFinished();
         });
      }
      stopping.set(false);
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

   public boolean isExecuting() {
      ScheduledFuture<?> scheduledFuture = this.pollingFutureRef.get();
      return scheduledFuture != null && !scheduledFuture.isCancelled() && !scheduledFuture.isDone();
   }

   public ActionGroup createActionsGroup() {
      DQLExecutionService service = this;
      DefaultActionGroup group = new DefaultActionGroup();
      group.add(new AnAction(DQLBundle.message("components.tableResults.actions.stopExecution"), null, AllIcons.Actions.StopRefresh) {
         @Override
         public void actionPerformed(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(false);
            ApplicationManager.getApplication().executeOnPooledThread(service::stopExecution);
            ActivityTracker.getInstance().inc();
         }

         @Override
         public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(!stopping.get());
            e.getPresentation().setVisible(isExecuting());
         }

         @Override
         public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
         }
      });

      group.add(new AnAction(DQLBundle.message("components.tableResults.actions.refreshExecution"), null, AllIcons.Actions.Refresh) {
         @Override
         public void actionPerformed(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(false);
            DQLServicesManager.getInstance(project).startExecution(new DQLExecutionService(processHandler, name, project, tenant, payload));
            ActivityTracker.getInstance().inc();
         }

         @Override
         public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setEnabled(!loading.get());
            e.getPresentation().setVisible(!isExecuting());
         }

         @Override
         public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
         }
      });
      group.addSeparator();
      group.add(new SaveAsFileAction(DQLBundle.message("components.tableResults.actions.exportAsJson"), null, service));
      group.add(new OpenQueryMetadataAction(DQLBundle.message("components.tableResults.actions.showMetadata"), null, service));
      group.addSeparator();
      group.add(new AnAction(DQLBundle.message("components.tableResults.actions.closeExecution"), null, AllIcons.Actions.Close) {
         @Override
         public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            DQLServicesManager.getInstance(project).stopExecution(service);
         }
      });
      return group;
   }

   private void executeApiCall(@NotNull ExecuteApiCall apiCall) {
      try {
         apiCall.execute();
         ActivityTracker.getInstance().inc();
      } catch (InterruptedException | IOException e) {
         processHandler.notifyExecutionError(e.getMessage());
         if (pollingFutureRef.get() != null) {
            pollingFutureRef.get().cancel(true);
         }
         resultPanel.showError(null, e);
      } catch (DQLNotAuthorizedException e) {
         processHandler.notifyExecutionError(e.getMessage());
         if (pollingFutureRef.get() != null) {
            pollingFutureRef.get().cancel(true);
         }
         resultPanel.showError(e.getResponse(), e);
      } catch (DQLErrorResponseException e) {
         processHandler.notifyExecutionError(e.getMessage());
         if (pollingFutureRef.get() != null) {
            pollingFutureRef.get().cancel(true);
         }
         resultPanel.showError(e.getResponse(), e);
      }
      finally {
         loading.set(false);
         stopping.set(false);
      }
   }

   private interface ExecuteApiCall {
      void execute() throws InterruptedException, IOException, DQLNotAuthorizedException, DQLErrorResponseException;
   }
}

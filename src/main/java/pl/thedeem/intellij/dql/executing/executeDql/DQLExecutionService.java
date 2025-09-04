package pl.thedeem.intellij.dql.executing.executeDql;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.navigation.ItemPresentation;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.DQLUtil;
import pl.thedeem.intellij.dql.DynatraceQueryLanguage;
import pl.thedeem.intellij.dql.components.DQLResultPanel;
import pl.thedeem.intellij.dql.components.actions.SaveAsFileAction;
import pl.thedeem.intellij.dql.executing.DQLExecutionUtil;
import pl.thedeem.intellij.dql.executing.DQLParsedQuery;
import pl.thedeem.intellij.dql.executing.executeDql.runConfiguration.DQLProcessHandler;
import pl.thedeem.intellij.dql.executing.services.DQLServicesManager;
import pl.thedeem.intellij.dql.fileProviders.DQLMetadataVirtualFile;
import pl.thedeem.intellij.dql.psi.DQLItemPresentation;
import pl.thedeem.intellij.dql.sdk.DynatraceRestClient;
import pl.thedeem.intellij.dql.sdk.errors.DQLApiException;
import pl.thedeem.intellij.dql.sdk.model.DQLExecutePayload;
import pl.thedeem.intellij.dql.sdk.model.DQLExecuteResponse;
import pl.thedeem.intellij.dql.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.intellij.openapi.diagnostic.Logger;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

public class DQLExecutionService {
   private static final Logger logger = Logger.getInstance(DQLExecutionService.class);
   private final static int POLL_INTERVAL = 200;

   private final DQLProcessHandler processHandler;
   private final Project project;
   private final DQLExecutePayload payload;
   private final DQLResultPanel resultPanel;
   private final String name;
   private final ActionGroup actionGroup;
   private final AtomicReference<ScheduledFuture<?>> pollingFutureRef = new AtomicReference<>(null);
   private final AtomicReference<Boolean> stopping = new AtomicReference<>(false);
   private final AtomicReference<Boolean> loading = new AtomicReference<>(false);
   private final DynatraceTenant tenant;
   private final String dqlFile;
   private String requestToken;
   private DQLPollResponse result;
   private final LocalDateTime executionTime;

   public DQLExecutionService(
       @NotNull DQLProcessHandler processHandler,
       @NotNull String name,
       @NotNull Project project,
       @NotNull DynatraceTenant tenant,
       @Nullable String dqlFile,
       @NotNull DQLExecutePayload payload
   ) {
      this.processHandler = processHandler;
      this.project = project;
      this.tenant = tenant;
      this.payload = payload;
      this.resultPanel = new DQLResultPanel(project);
      this.name = name;
      this.dqlFile = dqlFile;
      this.actionGroup = createActionsGroup();
      this.executionTime = LocalDateTime.now();
   }

   public @NotNull String getName() {
      return name;
   }

   public @NotNull LocalDateTime getExecutionTime() {
      return executionTime;
   }

   public void execute() {
      String apiToken = PasswordSafe.getInstance().getPassword(DQLUtil.createCredentialAttributes(tenant.getCredentialId()));
      DynatraceRestClient client = new DynatraceRestClient(tenant.getUrl());
      if (dqlFile != null) {
         PsiFile psiFile = ReadAction.compute(() -> DQLUtil.openFile(project, Path.of(DQLExecutionUtil.getProjectAbsolutePath(this.dqlFile, project)).toString()));
         if (psiFile != null) {
            DQLParsedQuery parsedQuery = new DQLParsedQuery(psiFile);
            payload.setQuery(parsedQuery.getParsedQuery());
         }
         else {
            Notifications.Bus.notify(new Notification(
                DynatraceQueryLanguage.DQL_ID,
                DQLBundle.message("components.tableResults.actions.errors.missingFile.title"),
                DQLBundle.message("components.tableResults.actions.errors.missingFile.description", dqlFile),
                NotificationType.ERROR
            ), project);
            return;
         }
      }
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
            DQLServicesManager.getInstance(project).startExecution(new DQLExecutionService(processHandler, name, project, tenant, dqlFile, payload));
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
      group.add(new SaveAsFileAction(DQLBundle.message("components.tableResults.actions.exportAsJson"), null, service, project));
      group.add(new AnAction(DQLBundle.message("components.tableResults.actions.showMetadata"), null, AllIcons.Nodes.DataTables) {
         @Override
         public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
            FileEditorManager.getInstance(project).openFile(new DQLMetadataVirtualFile(
                    DQLBundle.message("components.queryDetails.fileName", getName()),
                    result.getResult().getGrailMetadata(),
                    getExecutionTime()
                ), true
            );
         }

         @Override
         public void update(@NotNull AnActionEvent e) {
            Presentation presentation = e.getPresentation();
            presentation.setEnabledAndVisible(service.getResult() != null);
         }

         @Override
         public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.EDT;
         }
      });

      group.addSeparator();
      group.add(new DefaultActionGroup(tenant.getName(), true) {
         @Override
         public void update(@NotNull AnActionEvent e) {
            setPopup(true);
            Presentation presentation = e.getPresentation();
            presentation.setText(tenant.getName());
            presentation.setIcon(DQLIcon.DYNATRACE_LOGO);
            presentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true);
            presentation.setDescription(DQLBundle.message("components.tableResults.actions.selectTenant"));
            presentation.setEnabled(!loading.get() && !stopping.get());
         }

         @Override
         public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
         }

         @Override
         public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
            List<DynatraceTenant> tenants = DynatraceTenantsService.getInstance().getTenants();
            return tenants.stream()
                .filter(option -> !Objects.equals(option.getName(), tenant.getName()))
                .map(option -> new AnAction(option.getName()) {
                   @Override
                   public void actionPerformed(@NotNull AnActionEvent e) {
                      DQLServicesManager.getInstance(project).startExecution(new DQLExecutionService(processHandler, name, project, option, dqlFile, payload));
                      ActivityTracker.getInstance().inc();
                   }
                })
                .toArray(AnAction[]::new);
         }
      });

      if (dqlFile != null) {
         group.add(new AnAction(DQLBundle.message("components.tableResults.actions.openRelatedFile"), dqlFile, AllIcons.Ide.ConfigFile) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
               PsiFile psiFile = DQLUtil.openFile(project, Path.of(DQLExecutionUtil.getProjectAbsolutePath(dqlFile, project)).toString());
               Editor editor = psiFile != null ? FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, psiFile.getVirtualFile(), 0), true) : null;
               if (psiFile == null || editor == null ) {
                  Notifications.Bus.notify(new Notification(
                      DynatraceQueryLanguage.DQL_ID,
                      DQLBundle.message("components.tableResults.actions.errors.missingFile.title"),
                      DQLBundle.message("components.tableResults.actions.errors.missingFile.description", dqlFile),
                      NotificationType.ERROR
                  ), project);
               }
            }
         });
      }

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
      } catch (InterruptedException | IOException | DQLApiException e) {
         logger.warn("An error was found when executing the API call to the DT tenant. Cancelling the execution...", e);
         processHandler.notifyExecutionError(e.getMessage());
         if (pollingFutureRef.get() != null) {
            pollingFutureRef.get().cancel(true);
         }
         resultPanel.showError(e);
      } finally {
         loading.set(false);
         stopping.set(false);
      }
   }

   private interface ExecuteApiCall {
      void execute() throws InterruptedException, IOException, DQLApiException;
   }
}

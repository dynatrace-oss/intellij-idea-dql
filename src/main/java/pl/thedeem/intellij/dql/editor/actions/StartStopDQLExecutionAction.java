package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.icons.AllIcons;
import com.intellij.ide.ActivityTracker;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.common.services.ManagedService;
import pl.thedeem.intellij.common.services.ProjectServicesManager;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.actions.ExecuteDQLQueryAction;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;
import pl.thedeem.intellij.dql.exec.DQLProcessHandler;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;

import java.util.List;
import java.util.Objects;

public class StartStopDQLExecutionAction extends AnAction {
    public StartStopDQLExecutionAction() {
        super("", null, AllIcons.Actions.Execute);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        DQLExecutionService service = e.getData(DQLExecutionService.EXECUTION_SERVICE);

        if (service != null) {
            update(e, service);
            return;
        }

        Project project = e.getProject();
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (project == null || file == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        List<ManagedService> services = getRunningServices(project, file);
        if (!services.isEmpty()) {
            presentation.setText(DQLBundle.message("editor.action.stopExecution.title"));
            presentation.setIcon(AllIcons.Run.Stop);
        } else {
            presentation.setText(DQLBundle.message("editor.action.execute.title"));
            presentation.setIcon(AllIcons.Actions.Execute);
        }
        DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance();
        QueryConfiguration configuration = configurationService.getQueryConfiguration(file);
        if (StringUtil.isEmpty(configuration.tenant())) {
            presentation.setEnabled(false);
        }
    }

    protected void update(@NotNull AnActionEvent e, @NotNull DQLExecutionService service) {
        Presentation presentation = e.getPresentation();
        QueryConfiguration configuration = service.getConfiguration();
        if (StringUtil.isEmpty(configuration.tenant())) {
            presentation.setEnabled(false);
            return;
        }
        if (service.isRunning()) {
            presentation.setText(DQLBundle.message("editor.action.stopExecution.title"));
            presentation.setIcon(AllIcons.Run.Stop);
        } else {
            presentation.setText(DQLBundle.message("editor.action.rerun.title"));
            presentation.setIcon(AllIcons.Actions.Rerun);
        }
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        DQLExecutionService service = e.getData(DQLExecutionService.EXECUTION_SERVICE);
        if (service != null) {
            if (service.isRunning()) {
                stopService(service);
            } else {
                startService(project, new DQLExecutionService(
                        service.getServiceId(),
                        Objects.requireNonNullElseGet(
                                e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION),
                                service::getConfiguration
                        ),
                        project,
                        new DQLProcessHandler()
                ));
            }
            return;
        }

        PsiFile file = getRelatedPsiFile(e);
        if (file == null) {
            return;
        }

        List<ManagedService> runningServices = getRunningServices(project, file);
        if (!runningServices.isEmpty()) {
            stopService(runningServices);
        } else {
            startService(project, e, file);
        }
    }

    private void stopService(@NotNull List<ManagedService> runningServices) {
        for (ManagedService runningService : runningServices) {
            if (runningService instanceof DQLExecutionService executionService) {
                stopService(executionService);
            }
        }
    }

    private void startService(@NotNull Project project, @NotNull AnActionEvent e, @NotNull PsiFile file) {
        Editor editor = getEditor(e);
        DQLQueryConfigurationService configurationService = DQLQueryConfigurationService.getInstance();
        QueryConfiguration config = Objects.requireNonNullElseGet(
                e.getData(DQLQueryConfigurationService.DATA_QUERY_CONFIGURATION),
                () -> configurationService.getQueryConfiguration(file)
        );
        configurationService.getQueryFromEditorContext(file, editor, (query) -> {
            QueryConfiguration configuration = config.copy();
            configuration.setQuery(query);
            e.getPresentation().setEnabled(false);
            DQLExecutionService newService = new DQLExecutionService(
                    DQLBundle.message(
                            "services.executeDQL.serviceName",
                            Objects.requireNonNullElseGet(
                                    e.getData(ExecuteDQLQueryAction.PREFERRED_EXECUTION_NAME),
                                    file::getName
                            )
                    ),
                    configuration,
                    project,
                    new DQLProcessHandler()
            );
            startService(project, newService);
        });
    }

    private void stopService(@NotNull DQLExecutionService service) {
        service.stopExecution();
    }

    private void startService(@NotNull Project project, @NotNull DQLExecutionService service) {
        ProjectServicesManager.getInstance(project).registerService(service);
        ActivityTracker.getInstance().inc();
        service.startExecution();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private @NotNull List<ManagedService> getRunningServices(@NotNull Project project, @NotNull PsiFile file) {
        return ProjectServicesManager.getInstance(project).find((s) -> {
            if (s instanceof DQLExecutionService executionService) {
                QueryConfiguration configuration = executionService.getConfiguration();
                if (configuration.originalFile() != null) {
                    VirtualFile relatedFile = IntelliJUtils.getProjectRelativeFile(configuration.originalFile(), project);
                    if (relatedFile != null && relatedFile.getPath().equals(file.getVirtualFile().getPath())) {
                        return executionService.isRunning();
                    }
                }
            }
            return false;
        });
    }

    private @Nullable PsiFile getRelatedPsiFile(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return null;
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
            if (virtualFile != null) {
                psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            }

        }
        if (psiFile == null) {
            return null;
        }
        if (!DQLFileType.INSTANCE.equals(psiFile.getFileType())) {
            InjectedLanguageManager manager = InjectedLanguageManager.getInstance(psiFile.getProject());
            Editor editor = getEditor(e);
            if (editor == null) {
                return null;
            }
            PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
            PsiElement injectedElement = manager.findInjectedElementAt(psiFile, editor.getCaretModel().getOffset());
            if (injectedElement != null && DQLFileType.INSTANCE.equals(injectedElement.getContainingFile().getFileType())) {
                return injectedElement.getContainingFile();
            } else if (psiElement != null) {
                return DQLFileType.INSTANCE.equals(psiElement.getContainingFile().getFileType()) ? psiElement.getContainingFile() : null;
            }
        }
        return psiFile;
    }

    public @Nullable Editor getEditor(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            FileEditor fileEditor = e.getData(PlatformCoreDataKeys.FILE_EDITOR);
            editor = fileEditor instanceof TextEditor textEditor ? textEditor.getEditor() : null;
        }
        return editor;
    }
}

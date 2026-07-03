package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLFileType;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.services.DQLPluginDisposable;
import pl.thedeem.intellij.dql.services.query.DQLQueryConfigurationService;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;
import pl.thedeem.intellij.dql.services.variables.components.DQLRuntimeVariableDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ManageVariablesAction extends AnAction {
    public ManageVariablesAction() {
        super(
                () -> DQLBundle.message("editor.action.manageVariables.title"),
                () -> DQLBundle.message("editor.action.manageVariables.description"),
                DQLIcon.DQL_VARIABLE
        );
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        if (e.isFromContextMenu()) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        Project project = e.getProject();
        if (project == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        PsiFile file = resolvePsiFile(e);
        if (file == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        DQLVariablesService service = DQLVariablesService.getInstance(e.getProject());
        presentation.setEnabledAndVisible(
                DQLFileType.INSTANCE.equals(file.getFileType())
                        && (!service.getUserDefinedVariables(file).isEmpty() || !service.getUndefinedVariables(file).isEmpty())
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiFile file = resolvePsiFile(e);
        if (project == null || file == null) {
            return;
        }
        DQLVariablesService service = DQLVariablesService.getInstance(project);
        ReadAction.nonBlocking(() -> collectManageableVariables(service, file))
                .inSmartMode(project)
                .expireWith(DQLPluginDisposable.getInstance(project))
                .finishOnUiThread(ModalityState.defaultModalityState(), variables -> {
                    DQLRuntimeVariableDialog dialog = new DQLRuntimeVariableDialog(project, variables);
                    if (dialog.showAndGet()) {
                        service.updateUserDefinedVariables(file, dialog.getResult());
                    }
                })
                .submit(AppExecutorUtil.getAppExecutorService());
    }

    private static @NotNull List<DQLVariablesService.VariableDefinition> collectManageableVariables(
            @NotNull DQLVariablesService service,
            @NotNull PsiFile file
    ) {
        List<DQLVariablesService.VariableDefinition> variables = new ArrayList<>(service.getUserDefinedVariables(file));
        variables.addAll(service.getUndefinedVariables(file).stream()
                .map(name -> new DQLVariablesService.VariableDefinition(name, null, Set.of()))
                .toList());
        return variables;
    }

    private static @Nullable PsiFile resolvePsiFile(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return null;
        }
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file != null) {
            return file;
        }
        String originalFile = e.getData(DQLQueryConfigurationService.DATA_ORIGINAL_FILE);
        if (originalFile == null) {
            return null;
        }
        VirtualFile virtualFile = IntelliJUtils.getProjectRelativeFile(originalFile, project);
        if (virtualFile == null) {
            return null;
        }
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}

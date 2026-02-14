package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ToggleExternalValidationAction extends ToggleAction {
    private static final Logger logger = Logger.getInstance(ToggleExternalValidationAction.class);

    public ToggleExternalValidationAction() {
        super(
                DQLBundle.message("editor.action.toggleExternalValidation.title"),
                DQLBundle.message("editor.action.toggleExternalValidation.description"),
                DQLIcon.EXTERNAL_VALIDATION_ENABLED
        );
    }

    @Override
    public boolean isSelected(@NotNull AnActionEvent e) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null) {
            return false;
        }

        Boolean userData = file.getUserData(DQLSettings.EXTERNAL_VALIDATION_ENABLED);
        return Objects.requireNonNullElseGet(userData, () -> DQLSettings.getInstance().isPerformingLiveValidationEnabled());
    }

    @Override
    public void setSelected(@NotNull AnActionEvent e, boolean state) {
        PsiFile file = e.getData(CommonDataKeys.PSI_FILE);
        if (file == null) {
            return;
        }

        file.putUserData(DQLSettings.EXTERNAL_VALIDATION_ENABLED, state);
        retriggerValidations(file);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    /*
     * The IntelliJ platform deprecated the "restart" method in DaemonCodeAnalyzer in favor of a new method that accepts
     * an additional reason parameter.
     * Unfortunately, older versions of the platform do not have the new method, so we need to use reflection to call
     * the appropriate method based on the platform version.
     * This method will be replaced with "DaemonCodeAnalyzer.restart(file, reason)" once we drop support for
     * older platform versions.
     */
    private void retriggerValidations(@NotNull PsiFile file) {
        DaemonCodeAnalyzer analyzer = DaemonCodeAnalyzer.getInstance(file.getProject());
        try {
            Method restartMethod = analyzer.getClass().getMethod("restart", PsiFile.class, String.class);
            restartMethod.invoke(analyzer, file, "Validation settings have changed");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            try {
                Method restartMethod = analyzer.getClass().getMethod("restart", PsiFile.class);
                restartMethod.invoke(analyzer, file);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException error) {
                logger.warn("Could not retrigger code analysis after validation settings change", error);
            }
        }
    }
}


package pl.thedeem.intellij.dql.editor.actions;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.settings.DQLSettings;

import java.util.Objects;

public class ToggleExternalValidationAction extends ToggleAction {
    public ToggleExternalValidationAction() {
        super(
                DQLBundle.message("editor.action.toggleExternalValidation.title"),
                DQLBundle.message("editor.action.toggleExternalValidation.description"),
                AllIcons.General.InspectionsOK
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
        DaemonCodeAnalyzer.getInstance(file.getProject()).restart(file, "Validation settings have changed");
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}


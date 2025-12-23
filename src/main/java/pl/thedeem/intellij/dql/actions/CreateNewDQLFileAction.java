package pl.thedeem.intellij.dql.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;

public class CreateNewDQLFileAction extends CreateFileFromTemplateAction {
    private final static String DQL_FILE_TEMPLATE = "DQL File.dql";

    @Override
    protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory psiDirectory, CreateFileFromTemplateDialog.@NotNull Builder builder) {
        builder.setTitle(DQLBundle.message("action.DQL.NewDQLFile.title"))
                .addKind(
                        DQLBundle.message("action.DQL.NewDQLFile.kind"),
                        DQLIcon.DYNATRACE_LOGO,
                        DQL_FILE_TEMPLATE
                );
    }

    @Override
    protected @NlsContexts.Command String getActionName(PsiDirectory psiDirectory, @NonNls @NotNull String fileName, @NonNls String templateName) {
        return DQLBundle.message("action.DQL.NewDQLFile.description", fileName);
    }
}

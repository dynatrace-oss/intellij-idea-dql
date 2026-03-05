package pl.thedeem.intellij.dql.actions;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.services.notifications.DQLNotificationsService;

import javax.swing.*;
import java.awt.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Set;

public class CreateNewDQLFileAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiElement entryPoint = e.getData(CommonDataKeys.PSI_ELEMENT);

        if (project == null || !(entryPoint instanceof PsiDirectory directory)) return;

        CreateDQLFileDialog dialog = new CreateDQLFileDialog(project, directory);
        if (dialog.showAndGet()) {
            String fullName = dialog.getFileName();
            createFile(project, directory, fullName);
        }
    }

    private void createFile(Project project, PsiDirectory directory, String fileName) {
        FileTemplate template = FileTemplateManager.getInstance(project).getInternalTemplate("DQL File");
        try {
            FileTemplateUtil.createFromTemplate(template, fileName, null, directory);
        } catch (Exception e) {
            DQLNotificationsService.getInstance(project).showErrorNotification(
                    DQLBundle.message("action.DQL.NewDQLFile.errors.couldNotCreate.title"),
                    DQLBundle.message("action.DQL.NewDQLFile.errors.couldNotCreate.message", fileName, e.getMessage()),
                    project,
                    Set.of()
            );
        }
    }

    public static class CreateDQLFileDialog extends DialogWrapper {
        private final JBTextField fileNameField = new JBTextField();
        private final ComboBox<String> extensionCombo = new ComboBox<>(new String[]{".dql", ".dpl", ".dqlexpr", ".dqlpart"});
        private final PsiDirectory directory;

        public CreateDQLFileDialog(@Nullable Project project, @NotNull PsiDirectory directory) {
            super(project);
            this.directory = directory;

            fileNameField.setPreferredSize(new Dimension(JBUI.scale(250), fileNameField.getPreferredSize().height));
            setTitle(DQLBundle.message("action.DQL.NewDQLFile.title"));
            init();
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            return FormBuilder.createFormBuilder()
                    .addLabeledComponent(
                            new JBLabel(DQLBundle.message("action.DQL.NewDQLFile.form.name")),
                            new JBLabel(DQLBundle.message("action.DQL.NewDQLFile.form.extension"))
                    )
                    .addLabeledComponent(fileNameField, extensionCombo)
                    .getPanel();
        }

        @Nullable
        @Override
        protected ValidationInfo doValidate() {
            String name = fileNameField.getText().trim();
            if (name.isEmpty()) {
                return new ValidationInfo(DQLBundle.message("action.DQL.NewDQLFile.form.validation.nameEmpty"), fileNameField);
            }
            try {
                Paths.get(name);
            } catch (InvalidPathException | UnsupportedOperationException ex) {
                return new ValidationInfo(
                        DQLBundle.message("action.DQL.NewDQLFile.form.validation.invalidCharacters"),
                        fileNameField
                );
            }
            String fullName = getFileName();
            if (directory.findFile(fullName) != null) {
                return new ValidationInfo(DQLBundle.message("action.DQL.NewDQLFile.form.validation.alreadyExists"), fileNameField);
            }
            return null;
        }

        @Override
        public JComponent getPreferredFocusedComponent() {
            return fileNameField;
        }

        public String getFileName() {
            String name = fileNameField.getText().trim();
            String ext = String.valueOf(extensionCombo.getSelectedItem());
            if (name.endsWith(ext)) {
                return name;
            }
            return name + ext;
        }
    }
}
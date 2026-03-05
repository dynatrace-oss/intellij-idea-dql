package pl.thedeem.intellij.dql.actions;

import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.lang.LangBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.InputValidatorEx;
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

import javax.swing.*;
import java.awt.*;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

public class CreateNewDQLFileAction extends CreateFileAction {
    @Override
    protected void invokeDialog(@NotNull Project project, @NotNull PsiDirectory directory, @NotNull Consumer<? super PsiElement[]> elementsConsumer) {
        CreateElementActionBase.MyInputValidator validator = new MyValidator(project, directory);
        CreateDQLFileDialog dialog = new CreateDQLFileDialog(project, directory, validator);

        if (dialog.showAndGet()) {
            elementsConsumer.accept(validator.getCreatedElements());
        }
    }

    private static class CreateDQLFileDialog extends DialogWrapper {
        private final JBTextField fileNameField = new JBTextField();
        private final ComboBox<String> extensionCombo = new ComboBox<>(new String[]{".dql", ".dpl", ".dqlexpr", ".dqlpart"});
        private final PsiDirectory directory;
        private final CreateElementActionBase.MyInputValidator validator;

        CreateDQLFileDialog(@Nullable Project project, @NotNull PsiDirectory directory, @NotNull CreateElementActionBase.MyInputValidator validator) {
            super(project);
            this.directory = directory;
            this.validator = validator;

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
            if (!validator.checkInput(fullName) || !validator.canClose(fullName)) {
                String errorMessage = validator instanceof InputValidatorEx ? ((InputValidatorEx) validator).getErrorText(name) : LangBundle.message("incorrect.name");
                return new ValidationInfo(
                        Objects.requireNonNullElseGet(
                                errorMessage,
                                () -> DQLBundle.message("action.DQL.NewDQLFile.form.validation.unknownError")
                        ),
                        fileNameField
                );
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
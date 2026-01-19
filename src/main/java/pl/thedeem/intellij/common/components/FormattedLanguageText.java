package pl.thedeem.intellij.common.components;

import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.ui.EditorTextField;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.AsyncProcessIcon;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class FormattedLanguageText extends JComponent {
    private final AsyncProcessIcon processIcon;
    private final boolean isViewer;
    private EditorTextField editorField;

    public FormattedLanguageText(boolean isViewer) {
        this.isViewer = isViewer;
        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(JBUI.Borders.empty());
        JPanel spinner = new JPanel(new GridBagLayout());
        spinner.setOpaque(false);
        spinner.setBorder(JBUI.Borders.empty());
        processIcon = new AsyncProcessIcon("PreparingFile");
        spinner.add(processIcon);
        add(spinner, BorderLayout.CENTER);
    }

    public void showResult(@Nullable String content, @NotNull FileType type, @NotNull Project project) {
        ProgressManager processManager = ProgressManager.getInstance();
        processManager.run(new Task.Backgroundable(project, DQLBundle.message("components.preparingView"), true) {
            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                String c = Objects.requireNonNullElse(content, "");
                PsiFile file = ReadAction.compute(() -> PsiFileFactory.getInstance(project).createFileFromText("temporary" + type.getDefaultExtension(), type, c));
                WriteCommandAction.runWriteCommandAction(project, (Computable<Object>) () -> {
                    CodeStyleManager.getInstance(project).reformat(file, true);
                    PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
                    manager.commitDocument(file.getFileDocument());
                    return null;
                });
                ReadAction.nonBlocking(() -> {
                    CodeFoldingManager.getInstance(project).buildInitialFoldings(file.getFileDocument());
                    return file;
                }).submit(AppExecutorUtil.getAppExecutorService());
                editorField = IntelliJUtils.createEditorPanel(file, isViewer);
                editorField.addSettingsProvider(editor -> WriteCommandAction.runWriteCommandAction(project, (Computable<Object>) () -> {
                    CodeFoldingManager.getInstance(project).updateFoldRegions(editor);
                    return null;
                }));
            }

            @Override
            public void onSuccess() {
                if (project.isDisposed() || editorField == null) {
                    return;
                }
                removeAll();
                add(editorField, BorderLayout.CENTER);
            }

            @Override
            public void onCancel() {
                removeAll();
            }

            @Override
            public void onFinished() {
                processIcon.suspend();
                processIcon.dispose();
            }
        });
    }

    public String getText() {
        if (editorField == null) {
            return null;
        }
        return editorField.getText();
    }
}

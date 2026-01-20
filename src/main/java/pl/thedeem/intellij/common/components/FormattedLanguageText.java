package pl.thedeem.intellij.common.components;

import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.EditorTextField;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.concurrent.Callable;

public class FormattedLanguageText extends BorderLayoutPanel implements Disposable {
    private final LoadingPanel processIcon;
    private final EditorTextField editorField;

    public FormattedLanguageText(@NotNull Language language, @NotNull Project project, boolean isViewer) {
        setOpaque(false);
        setBorder(JBUI.Borders.empty());
        JPanel spinner = new JPanel(new GridBagLayout());
        spinner.setOpaque(false);
        spinner.setBorder(JBUI.Borders.empty());
        processIcon = new LoadingPanel(DQLBundle.message("components.preparingView"));
        addToCenter(processIcon);
        editorField = IntelliJUtils.createEditorPanel(project, language, isViewer);
        addToCenter(editorField);
        editorField.setVisible(false);
    }

    public void showResult(@NotNull Callable<String> content) {
        Project project = editorField.getProject();
        ProgressManager processManager = ProgressManager.getInstance();
        processManager.run(new Task.Backgroundable(project, DQLBundle.message("components.preparingView"), true) {
            private String resultText;

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    resultText = content.call();
                } catch (Exception e) {
                    progressIndicator.cancel();
                }
            }

            @Override
            public void onSuccess() {
                if (project.isDisposed()) {
                    return;
                }

                WriteCommandAction.runWriteCommandAction(project, () -> {
                    editorField.setText(Objects.requireNonNullElse(resultText, ""));
                    setEditorFieldVisible();
                });

                PsiDocumentManager.getInstance(project).performLaterWhenAllCommitted(() -> {
                    if (editorField.getEditor() == null) {
                        return;
                    }
                    ReadAction.nonBlocking(() -> {
                                if (editorField.getEditor() != null) {
                                    return CodeFoldingManager.getInstance(project).updateFoldRegionsAsync(editorField.getEditor(), true);
                                }
                                return null;
                            })
                            .expireWith(FormattedLanguageText.this)
                            .finishOnUiThread(ModalityState.any(), runnable -> {
                                if (runnable != null) {
                                    runnable.run();
                                }
                            }).submit(AppExecutorUtil.getAppExecutorService());
                });
            }
        });
    }

    private void setEditorFieldVisible() {
        editorField.setVisible(true);
        processIcon.dispose();
        processIcon.setVisible(false);
        revalidate();
        repaint();
    }

    public @NotNull String getText() {
        return editorField.getText();
    }

    @Override
    public void dispose() {
        processIcon.dispose();
    }
}

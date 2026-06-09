package pl.thedeem.intellij.common.components;

import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;

import java.util.Objects;
import java.util.concurrent.Callable;

public class FormattedLanguageText extends BorderLayoutPanel implements Disposable {
    private final LoadingPanel processIcon;
    private final TextEditor textEditor;

    public FormattedLanguageText(@NotNull Language language, @NotNull Project project, boolean isViewer) {
        withBorder(JBUI.Borders.empty()).andTransparent();
        processIcon = new LoadingPanel(DQLBundle.message("components.preparingView"));
        addToCenter(processIcon);

        LanguageFileType languageFileType = language.getAssociatedFileType();
        FileType fileType = languageFileType != null ? languageFileType : PlainTextFileType.INSTANCE;
        LightVirtualFile virtualFile = new LightVirtualFile("result." + fileType.getDefaultExtension(), fileType, "");

        textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, virtualFile);
        if (isViewer) {
            ((EditorEx) textEditor.getEditor()).setViewer(true);
        }
        addToCenter(textEditor.getComponent());
        textEditor.getComponent().setVisible(false);
    }

    public void showResult(@NotNull Callable<String> content) {
        Project project = textEditor.getEditor().getProject();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, DQLBundle.message("components.preparingView"), true) {
            private String resultText;

            @Override
            public void run(@NotNull ProgressIndicator progressIndicator) {
                try {
                    resultText = content.call();
                } catch (Exception e) {
                    progressIndicator.cancel();
                    resultText = null;
                }
            }

            @Override
            public void onSuccess() {
                if (project == null || project.isDisposed()) {
                    return;
                }
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    textEditor.getEditor().getDocument().setText(StringUtil.convertLineSeparators(Objects.requireNonNullElse(resultText, "")));
                    setEditorVisible();
                });
            }
        });
    }

    private void setEditorVisible() {
        textEditor.getComponent().setVisible(true);
        processIcon.dispose();
        processIcon.setVisible(false);
        revalidate();
        repaint();
    }

    public @NotNull String getText() {
        return textEditor.getEditor().getDocument().getText();
    }

    @Override
    public void dispose() {
        processIcon.dispose();
        TextEditorProvider.getInstance().disposeEditor(textEditor);
    }
}

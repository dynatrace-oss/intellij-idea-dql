package pl.thedeem.intellij.common.components;

import com.intellij.codeInsight.folding.CodeFoldingManager;
import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;

import java.util.Objects;
import java.util.concurrent.Callable;

public class FormattedLanguageText extends BorderLayoutPanel implements Disposable {
    private final LoadingPanel processIcon;
    private final Project project;
    private final Editor editor;

    public FormattedLanguageText(@NotNull Language language, @NotNull Project project, boolean isViewer) {
        withBorder(JBUI.Borders.empty()).andTransparent();
        this.project = project;
        processIcon = new LoadingPanel(DQLBundle.message("components.preparingView"));
        addToCenter(processIcon);

        LanguageFileType languageFileType = language.getAssociatedFileType();
        FileType fileType = languageFileType != null ? languageFileType : PlainTextFileType.INSTANCE;
        LightVirtualFile virtualFile = new LightVirtualFile("result." + fileType.getDefaultExtension(), fileType, "");

        Document document = Objects.requireNonNull(FileDocumentManager.getInstance().getDocument(virtualFile));
        editor = EditorFactory.getInstance().createEditor(document, project, fileType, isViewer);
        EditorEx editorEx = (EditorEx) editor;
        editorEx.setFile(virtualFile);
        editorEx.getSettings().setFoldingOutlineShown(true);
        editorEx.getFoldingModel().setFoldingEnabled(true);

        addToCenter(editor.getComponent());
        editor.getComponent().setVisible(false);
    }

    public void showResult(@NotNull Callable<String> content) {
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
                if (project.isDisposed()) return;
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    editor.getDocument().setText(StringUtil.convertLineSeparators(Objects.requireNonNullElse(resultText, "")));
                    setEditorVisible();
                });
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (!editor.isDisposed()) {
                        CodeFoldingManager.getInstance(project).updateFoldRegionsAsync(editor, true);
                    }
                });
            }
        });
    }

    private void setEditorVisible() {
        editor.getComponent().setVisible(true);
        processIcon.dispose();
        processIcon.setVisible(false);
        revalidate();
        repaint();
    }

    public @NotNull String getText() {
        return editor.getDocument().getText();
    }

    @Override
    public void dispose() {
        processIcon.dispose();
        EditorFactory.getInstance().releaseEditor(editor);
    }
}

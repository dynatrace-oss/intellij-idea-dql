package pl.thedeem.intellij.common.components;

import com.intellij.lang.Language;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;

import java.util.Objects;
import java.util.concurrent.Callable;

public class FormattedLanguageText extends BorderLayoutPanel implements Disposable {
    private final LoadingPanel processIcon;
    private final Project project;
    private final Language language;
    private final String fileExtension;
    private final boolean isViewer;
    private @Nullable TextEditor currentEditor;

    public FormattedLanguageText(@NotNull Language language, @NotNull Project project, boolean isViewer) {
        withBorder(JBUI.Borders.empty()).andTransparent();
        this.project = project;
        this.language = language;
        this.isViewer = isViewer;
        LanguageFileType languageFileType = language.getAssociatedFileType();
        this.fileExtension = languageFileType != null ? languageFileType.getDefaultExtension() : "txt";
        processIcon = new LoadingPanel(DQLBundle.message("components.preparingView"));
        addToCenter(processIcon);
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
                String text = StringUtil.convertLineSeparators(Objects.requireNonNullElse(resultText, ""));

                disposeCurrentEditor();

                PsiFile psiFile = PsiFileFactory.getInstance(project)
                        .createFileFromText("result." + fileExtension, language, text);
                VirtualFile vf = psiFile.getVirtualFile();
                if (vf == null) {
                    return;
                }
                TextEditor textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, vf);
                if (isViewer) {
                    ((EditorEx) textEditor.getEditor()).setViewer(true);
                }
                currentEditor = textEditor;

                addToCenter(textEditor.getComponent());
                processIcon.dispose();
                processIcon.setVisible(false);
                revalidate();
                repaint();
            }
        });
    }

    private void disposeCurrentEditor() {
        if (currentEditor != null) {
            remove(currentEditor.getComponent());
            TextEditorProvider.getInstance().disposeEditor(currentEditor);
            currentEditor = null;
        }
    }

    public @NotNull String getText() {
        return currentEditor != null ? currentEditor.getEditor().getDocument().getText() : "";
    }

    @Override
    public void dispose() {
        processIcon.dispose();
        disposeCurrentEditor();
    }
}

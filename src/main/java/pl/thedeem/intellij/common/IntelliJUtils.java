package pl.thedeem.intellij.common;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.impl.EditConfigurationsDialog;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.executing.runConfiguration.ExecuteDQLRunConfiguration;

import javax.swing.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class IntelliJUtils {
    public static @NotNull EditorTextField createEditorPanel(@NotNull PsiFile file, boolean isViewer) {
        EditorTextField result = new EditorTextField(file.getFileDocument(), file.getProject(), file.getFileType()) {
            @Override
            protected @NotNull EditorEx createEditor() {
                EditorEx editor = super.createEditor();
                editor.setViewer(isViewer);
                editor.setOneLineMode(false);
                editor.getScrollPane().setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                editor.getScrollPane().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                editor.setColorsScheme(EditorColorsManager.getInstance().getGlobalScheme());
                editor.getFoldingModel().setFoldingEnabled(true);
                EditorSettings settings = editor.getSettings();
                settings.setLineNumbersShown(true);
                settings.setFoldingOutlineShown(true);
                settings.setAutoCodeFoldingEnabled(true);
                settings.setWhitespacesShown(false);
                settings.setIndentGuidesShown(true);
                settings.setLineMarkerAreaShown(false);
                editor.setBorder(BorderFactory.createEmptyBorder());
                return editor;
            }
        };
        result.setBorder(BorderFactory.createEmptyBorder());
        result.setOpaque(false);
        return result;
    }

    public static void openRunConfiguration(@NotNull Project project) {
        try {
            DataContext dataContext = DataManager.getInstance().getDataContextFromFocusAsync().blockingGet(200);
            EditConfigurationsDialog dialog = new EditConfigurationsDialog(project, dataContext);
            dialog.show();
        } catch (TimeoutException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static @Nullable RunnerAndConfigurationSettings findConfiguration(@NotNull String name, @NotNull RunManager runManager) {
        for (RunnerAndConfigurationSettings settings : runManager.getAllSettings()) {
            if (settings.getConfiguration() instanceof ExecuteDQLRunConfiguration dqlSettings && dqlSettings.getName().equals(name)) {
                return settings;
            }
        }
        return null;
    }

    public static @Nullable String getRelativeProjectPath(@NotNull VirtualFile file, @NotNull Project project) {
        VirtualFile baseDir = project.getProjectFile();
        if (baseDir == null) {
            return null;
        }
        return VfsUtilCore.getRelativePath(file, baseDir, '/');
    }
}

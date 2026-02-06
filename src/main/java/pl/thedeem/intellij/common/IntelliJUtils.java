package pl.thedeem.intellij.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.impl.EditConfigurationsDialog;
import com.intellij.ide.DataManager;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.*;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.exec.runConfiguration.ExecuteDQLRunConfiguration;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class IntelliJUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static @NotNull EditorTextField createEditorPanel(@NotNull Project project, @Nullable Language language, boolean isViewer, @NotNull List<EditorCustomization> customizations) {
        EditorTextField editorField = EditorTextFieldProvider.getInstance().getEditorField(Objects.requireNonNullElse(language, PlainTextLanguage.INSTANCE), project, customizations);
        editorField.setViewer(isViewer);
        editorField.setBorder(JBUI.Borders.empty());
        editorField.setOpaque(false);
        return editorField;
    }

    public static @NotNull EditorTextField createEditorPanel(@NotNull Project project, @Nullable Language language, boolean isViewer) {
        return createEditorPanel(project, language, isViewer, List.of(
                new StandardEditorCustomization(),
                new EmptyBorderEditorCustomization(),
                SoftWrapsEditorCustomization.ENABLED
        ));
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
        VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
        if (baseDir == null) {
            return null;
        }
        return VfsUtilCore.getRelativePath(file, baseDir, '/');
    }

    public static @Nullable VirtualFile getProjectRelativeFile(@NotNull String relativePath, @NotNull Project project) {
        VirtualFile baseDir = ProjectUtil.guessProjectDir(project);
        if (baseDir == null) {
            return null;
        }
        return baseDir.findFileByRelativePath(FileUtil.toSystemIndependentName(relativePath));
    }

    public static class StandardEditorCustomization implements EditorCustomization {
        @Override
        public void customize(@NotNull EditorEx editor) {
            editor.setOneLineMode(false);
            editor.setColorsScheme(EditorColorsManager.getInstance().getGlobalScheme());
            editor.getFoldingModel().setFoldingEnabled(true);
            EditorSettings settings = editor.getSettings();
            settings.setLineNumbersShown(true);
            settings.setFoldingOutlineShown(true);
            settings.setAutoCodeFoldingEnabled(true);
            settings.setWhitespacesShown(false);
            settings.setIndentGuidesShown(true);
            settings.setLineMarkerAreaShown(false);
        }
    }

    public static class EmptyBorderEditorCustomization implements EditorCustomization {
        @Override
        public void customize(@NotNull EditorEx editor) {
            editor.setBorder(JBUI.Borders.empty());
        }
    }

    public static @Nullable String prettyPrintJson(@Nullable Object json) throws JsonProcessingException {
        if (json == null) {
            return "";
        }

        DefaultIndenter indenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);
        return mapper.writer(printer).writeValueAsString(json);
    }

    public static Icon scaleToBottomRight(@NotNull Icon base, @NotNull Icon original, float scale) {
        Icon scaled = IconUtil.scale(original, null, scale);

        return new LayeredIcon(2) {{
            setIcon(base, 0);
            setIcon(
                    scaled,
                    1,
                    base.getIconWidth() - scaled.getIconWidth(),
                    base.getIconHeight() - scaled.getIconHeight()
            );
        }};
    }

    public static void openSaveFileDialog(
            @NotNull String title,
            @NotNull String description,
            @NotNull String fileName,
            @NotNull Callable<String> contentCallback,
            @NotNull Project project
    ) {
        FileSaverDescriptor descriptor = getFileSaver(title, description);
        if (descriptor == null) {
            Messages.showErrorDialog(
                    DQLBundle.message("components.actions.saveAsFile.error.incompatibleVersion"),
                    DQLBundle.message("components.actions.saveAsFile.error.title")
            );
            return;
        }
        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
        VirtualFileWrapper fileWrapper = dialog.save(fileName);
        try {
            String content = contentCallback.call();
            if (fileWrapper != null && StringUtil.isNotEmpty(content)) {
                File file = fileWrapper.getFile();
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(content);
                }
            }
        } catch (Exception ex) {
            Messages.showErrorDialog(
                    DQLBundle.message("components.actions.saveAsFile.error.description", ex.getMessage()),
                    DQLBundle.message("components.actions.saveAsFile.error.title")
            );
        }
    }

    public static @Nullable FileSaverDescriptor getFileSaver(@NotNull String title, @NotNull String description) {
        try {
            Constructor<FileSaverDescriptor> ctor = FileSaverDescriptor.class.getConstructor(String.class, String.class, String.class);
            return ctor.newInstance(
                    title,
                    description,
                    "json"
            );
        } catch (Exception ignored) {
            try {
                Constructor<FileSaverDescriptor> ctor = FileSaverDescriptor.class.getConstructor(String.class, String.class, String[].class);
                return ctor.newInstance(
                        title,
                        description,
                        new String[]{"json"}
                );
            } catch (Exception ignored2) {
                return null;
            }
        }
    }
}

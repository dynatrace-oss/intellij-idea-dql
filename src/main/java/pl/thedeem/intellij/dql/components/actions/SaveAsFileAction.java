package pl.thedeem.intellij.dql.components.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.executing.executeDql.DQLExecutionService;
import pl.thedeem.intellij.dql.sdk.model.DQLResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class SaveAsFileAction extends AnAction {
    protected final static ObjectMapper mapper = new ObjectMapper();
    private final DQLExecutionService service;
    private final Project project;

    public SaveAsFileAction(@Nullable String text, @Nullable String description, @NotNull DQLExecutionService service, @NotNull Project project) {
        super(text, description, AllIcons.FileTypes.Json);
        this.service = service;
        this.project = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DQLResult result = service.getResult();
        if (result == null) {
            return;
        }

        FileSaverDescriptor descriptor = getFileSaver(
                DQLBundle.message("components.actions.saveAsFile.title"),
                DQLBundle.message("components.actions.saveAsFile.description")
        );

        if (descriptor == null) {
            Messages.showErrorDialog(
                    DQLBundle.message("components.actions.saveAsFile.error.incompatibleVersion"),
                    DQLBundle.message("components.actions.saveAsFile.error.title")
            );
            return;
        }

        FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
        VirtualFileWrapper fileWrapper = dialog.save(service.getPresentation().getPresentableText() + ".json");
        try {
            String content = mapper.writeValueAsString(result.getRecords());
            if (fileWrapper != null && StringUtil.isNotEmpty(content)) {
                File file = fileWrapper.getFile();

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(content);
                } catch (IOException ex) {
                    Messages.showErrorDialog(
                            DQLBundle.message("components.actions.saveAsFile.error.description", ex.getMessage()),
                            DQLBundle.message("components.actions.saveAsFile.error.title")
                    );
                }
            }
        } catch (JsonProcessingException ex) {
            Messages.showErrorDialog(
                    DQLBundle.message("components.actions.saveAsFile.error.description", ex.getMessage()),
                    DQLBundle.message("components.actions.saveAsFile.error.title")
            );
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setEnabledAndVisible(service.getResult() != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }

    // This method exists due to combability issues with older versions of IntelliJ
    private FileSaverDescriptor getFileSaver(String title, String description) {
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

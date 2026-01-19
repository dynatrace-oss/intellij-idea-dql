package pl.thedeem.intellij.dql.exec.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.common.sdk.model.DQLResult;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.exec.DQLExecutionService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;

public class SaveDQLResultsAsFileAction extends AbstractServiceAction {
    protected final static ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull DQLExecutionService service, @NotNull Project project) {
        DQLPollResponse response = service.getResult();
        DQLResult result = response != null ? response.getResult() : null;
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
    protected void update(@NotNull AnActionEvent e, @NotNull DQLExecutionService service, @NotNull Project project, @NotNull Presentation presentation) {
        presentation.setEnabledAndVisible(service.getResult() != null);
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

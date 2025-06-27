package pl.thedeem.intellij.dql.executing;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.impl.EditConfigurationsDialog;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.executing.executeDql.runConfiguration.ExecuteDQLRunConfiguration;

import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class DQLExecutionUtil {

    public static @NotNull String getProjectRelativePath(@NotNull String path, @NotNull Project project) {
        String rootPath = getProjectRootPath(project);
        if (rootPath == null) {
            return path;
        }
        return Path.of(rootPath).relativize(Path.of(path)).toString();
    }

    public static @NotNull String getProjectAbsolutePath(@NotNull String path, @NotNull Project project) {
        if (Path.of(path).isAbsolute()) {
            return path;
        }
        String rootPath = getProjectRootPath(project);
        if (rootPath == null) {
            return path;
        }
        return Path.of(rootPath).resolve(path).toString();
    }

    public static @Nullable String getProjectRootPath(@NotNull Project project) {
        ProjectRootManager manager = ProjectRootManager.getInstance(project);
        VirtualFile[] contentRoots = manager.getContentRoots();
        String relativePath = null;
        if (contentRoots.length > 0) {
            relativePath = contentRoots[0].getPath();
        }
        return relativePath;
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

    public static @Nullable RunnerAndConfigurationSettings findExistingSettings(@NotNull Project project, @NotNull PsiFile psiFile) {
        RunManager runManager = RunManager.getInstance(project);

        String runConfigName = getRunConfigName(psiFile);
        for (RunnerAndConfigurationSettings settings : runManager.getAllSettings()) {
            if (settings.getConfiguration() instanceof ExecuteDQLRunConfiguration && runConfigName.equals(settings.getConfiguration().getName())) {
                return settings;
            }
        }
        return null;
    }

    public static @NotNull String getRunConfigName(@NotNull PsiFile psiFile) {
        return DQLBundle.message("action.DQL.ExecuteScript.runConfigurationName", psiFile.getName());
    }
}

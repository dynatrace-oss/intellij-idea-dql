package pl.thedeem.intellij.dql.services.query;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.definition.model.QueryConfiguration;
import pl.thedeem.intellij.dql.exec.runConfiguration.ExecuteDQLRunConfiguration;
import pl.thedeem.intellij.dql.services.variables.DQLVariablesService;
import pl.thedeem.intellij.dql.settings.DQLSettings;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenant;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.util.Objects;

public class DQLQueryConfigurationServiceImpl implements DQLQueryConfigurationService {
    @Override
    public @NotNull QueryConfiguration getQueryConfiguration(@NotNull PsiFile file) {
        UserDataHolder dataHolder = Objects.requireNonNullElse(file.getVirtualFile(), file);
        QueryConfiguration configuration = dataHolder.getUserData(QUERY_CONFIGURATION);
        if (configuration == null) {
            configuration = Objects.requireNonNullElseGet(
                    createConfigurationFromRunManager(file.getProject(), file.getVirtualFile()),
                    () -> createDefaultConfiguration(file)
            );
        }
        if (configuration.tenant() != null) {
            DynatraceTenant tenant = DynatraceTenantsService.getInstance().findTenant(configuration.tenant());
            if (tenant == null) {
                configuration.setTenant(null);
            }
        }
        DQLVariablesService variablesService = DQLVariablesService.getInstance(file.getProject());
        configuration.setDefinedVariables(variablesService.getDefinedVariables(file));
        return configuration;
    }

    @Override
    public @NotNull QueryConfiguration createDefaultConfiguration(@NotNull PsiFile file) {
        QueryConfiguration result = createDefaultConfiguration(file.getProject(), file.getVirtualFile());
        result.setQuery(DQLQuerySelectorService.getInstance().getQueryText(file));
        return result;
    }

    @Override
    public @NotNull QueryConfiguration createDefaultConfiguration(@NotNull Project project, @Nullable VirtualFile virtualFile) {
        QueryConfiguration result = createDefaultConfiguration();
        if (virtualFile != null) {
            result.setOriginalFile(IntelliJUtils.getRelativeProjectPath(virtualFile, project));
        }
        return result;
    }

    @Override
    public @NotNull QueryConfiguration createDefaultConfiguration() {
        QueryConfiguration result = new QueryConfiguration();
        result.setTenant(DQLSettings.getInstance().getDefaultDynatraceTenant());
        return result;
    }

    private @Nullable QueryConfiguration createConfigurationFromRunManager(@NotNull Project project, @NotNull VirtualFile file) {
        if (file instanceof VirtualFileWindow) {
            return null;
        }
        RunManager runManager = RunManager.getInstance(project);
        String filePath = IntelliJUtils.getRelativeProjectPath(file, project);
        for (RunnerAndConfigurationSettings settings : runManager.getAllSettings()) {
            if (settings.getConfiguration() instanceof ExecuteDQLRunConfiguration dqlRunConfiguration && Objects.equals(filePath, dqlRunConfiguration.getDQLFile())) {
                return dqlRunConfiguration.getConfiguration();
            }
        }
        return null;
    }

    @Override
    public void updateConfiguration(@NotNull PsiFile file, @NotNull QueryConfiguration configuration) {
        UserDataHolder dataHolder = Objects.requireNonNullElse(file.getVirtualFile(), file);
        dataHolder.putUserData(QUERY_CONFIGURATION, configuration);
    }

    @Override
    public void updateConfiguration(@NotNull VirtualFile file, @NotNull QueryConfiguration configuration) {
        file.putUserData(QUERY_CONFIGURATION, configuration);
    }
}

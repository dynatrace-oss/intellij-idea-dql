package pl.thedeem.intellij.dql.services.query;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.IntelliJUtils;
import pl.thedeem.intellij.dql.exec.runConfiguration.ExecuteDQLRunConfiguration;
import pl.thedeem.intellij.dql.services.query.model.QueryConfiguration;
import pl.thedeem.intellij.dql.settings.DQLSettings;
import pl.thedeem.intellij.dql.settings.tenants.DynatraceTenantsService;

import java.util.Objects;

public class DQLQueryConfigurationServiceImpl implements DQLQueryConfigurationService {
    @Override
    public @NotNull QueryConfiguration getQueryConfiguration(@NotNull PsiFile file) {
        UserDataHolder dataHolder = Objects.requireNonNullElse(file.getVirtualFile(), file);
        VirtualFile virtualFile = file.getVirtualFile();

        QueryConfiguration fallback = virtualFile != null
                ? Objects.requireNonNullElseGet(
                        createConfigurationFromRunManager(file.getProject(), virtualFile),
                        () -> createDefaultConfiguration(file.getProject(), virtualFile))
                : createDefaultConfiguration(file.getProject(), null);

        String tenant = dataHolder.getUserData(TENANT);
        if (tenant != null && DynatraceTenantsService.getInstance().findTenant(tenant) == null) {
            tenant = null;
            dataHolder.putUserData(TENANT, null);
        }

        QueryConfiguration configuration = new QueryConfiguration();
        configuration.setTenant(firstNonNull(tenant, fallback.tenant()));
        configuration.setTimeframeStart(firstNonNull(dataHolder.getUserData(TIMEFRAME_START), fallback.timeframeStart()));
        configuration.setTimeframeEnd(firstNonNull(dataHolder.getUserData(TIMEFRAME_END), fallback.timeframeEnd()));
        configuration.setDefaultScanLimit(firstNonNull(dataHolder.getUserData(DEFAULT_SCAN_LIMIT), fallback.defaultScanLimit()));
        configuration.setMaxResultBytes(firstNonNull(dataHolder.getUserData(MAX_RESULT_BYTES), fallback.maxResultBytes()));
        configuration.setMaxResultRecords(firstNonNull(dataHolder.getUserData(MAX_RESULT_RECORDS), fallback.maxResultRecords()));
        configuration.setOriginalFile(firstNonNull(dataHolder.getUserData(ORIGINAL_FILE), fallback.originalFile()));
        configuration.setRunConfigName(firstNonNull(dataHolder.getUserData(RUN_CONFIG_NAME), fallback.runConfigName()));
        return configuration;
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

    @Override
    public @NotNull QueryConfiguration fromDataContext(@NotNull DataContext context) {
        QueryConfiguration result = new QueryConfiguration();
        result.setTenant(context.getData(DATA_TENANT));
        result.setTimeframeStart(context.getData(DATA_TIMEFRAME_START));
        result.setTimeframeEnd(context.getData(DATA_TIMEFRAME_END));
        result.setDefaultScanLimit(context.getData(DATA_DEFAULT_SCAN_LIMIT));
        result.setMaxResultBytes(context.getData(DATA_MAX_RESULT_BYTES));
        result.setMaxResultRecords(context.getData(DATA_MAX_RESULT_RECORDS));
        result.setOriginalFile(context.getData(DATA_ORIGINAL_FILE));
        result.setRunConfigName(context.getData(DATA_RUN_CONFIG_NAME));
        return result;
    }

    @Override
    public void updateConfiguration(@NotNull PsiFile file, @NotNull QueryConfiguration configuration) {
        writeToHolder(Objects.requireNonNullElse(file.getVirtualFile(), file), configuration);
    }

    @Override
    public void updateConfiguration(@NotNull VirtualFile file, @NotNull QueryConfiguration configuration) {
        writeToHolder(file, configuration);
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

    private static void writeToHolder(@NotNull UserDataHolder holder, @NotNull QueryConfiguration config) {
        holder.putUserData(TENANT, config.tenant());
        holder.putUserData(TIMEFRAME_START, config.timeframeStart());
        holder.putUserData(TIMEFRAME_END, config.timeframeEnd());
        holder.putUserData(DEFAULT_SCAN_LIMIT, config.defaultScanLimit());
        holder.putUserData(MAX_RESULT_BYTES, config.maxResultBytes());
        holder.putUserData(MAX_RESULT_RECORDS, config.maxResultRecords());
        holder.putUserData(ORIGINAL_FILE, config.originalFile());
        holder.putUserData(RUN_CONFIG_NAME, config.runConfigName());
    }

    private static <T> @Nullable T firstNonNull(@Nullable T a, @Nullable T b) {
        return a != null ? a : b;
    }
}

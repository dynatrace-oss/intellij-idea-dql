package pl.thedeem.intellij.dql.services.query;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.services.query.model.QueryConfiguration;

public interface DQLQueryConfigurationService {
    Key<String> TENANT = Key.create("DQL_TENANT");
    Key<String> TIMEFRAME_START = Key.create("DQL_TIMEFRAME_START");
    Key<String> TIMEFRAME_END = Key.create("DQL_TIMEFRAME_END");
    Key<Long> DEFAULT_SCAN_LIMIT = Key.create("DQL_DEFAULT_SCAN_LIMIT");
    Key<Long> MAX_RESULT_BYTES = Key.create("DQL_MAX_RESULT_BYTES");
    Key<Long> MAX_RESULT_RECORDS = Key.create("DQL_MAX_RESULT_RECORDS");
    Key<String> ORIGINAL_FILE = Key.create("DQL_ORIGINAL_FILE");
    Key<String> RUN_CONFIG_NAME = Key.create("DQL_RUN_CONFIG_NAME");

    DataKey<String> DATA_TENANT = DataKey.create("DQL_TENANT");
    DataKey<String> DATA_TIMEFRAME_START = DataKey.create("DQL_TIMEFRAME_START");
    DataKey<String> DATA_TIMEFRAME_END = DataKey.create("DQL_TIMEFRAME_END");
    DataKey<Long> DATA_DEFAULT_SCAN_LIMIT = DataKey.create("DQL_DEFAULT_SCAN_LIMIT");
    DataKey<Long> DATA_MAX_RESULT_BYTES = DataKey.create("DQL_MAX_RESULT_BYTES");
    DataKey<Long> DATA_MAX_RESULT_RECORDS = DataKey.create("DQL_MAX_RESULT_RECORDS");
    DataKey<String> DATA_ORIGINAL_FILE = DataKey.create("DQL_ORIGINAL_FILE");
    DataKey<String> DATA_RUN_CONFIG_NAME = DataKey.create("DQL_RUN_CONFIG_NAME");

    static @NotNull DQLQueryConfigurationService getInstance() {
        return ApplicationManager.getApplication().getService(DQLQueryConfigurationService.class);
    }

    @NotNull QueryConfiguration getQueryConfiguration(@NotNull PsiFile file);

    @NotNull QueryConfiguration createDefaultConfiguration(@NotNull Project project, @Nullable VirtualFile virtualFile);

    @NotNull QueryConfiguration createDefaultConfiguration();

    @NotNull QueryConfiguration fromDataContext(@NotNull DataContext context);

    void updateConfiguration(@NotNull PsiFile file, @NotNull QueryConfiguration configuration);

    void updateConfiguration(@NotNull VirtualFile file, @NotNull QueryConfiguration configuration);
}

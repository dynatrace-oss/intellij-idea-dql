package pl.thedeem.intellij.dql.executing.executeDql;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;

public class ExecuteDQLRunConfiguration extends RunConfigurationBase<ExecuteDQLRunConfigurationOptions> {
    protected ExecuteDQLRunConfiguration(Project project,
                                         ConfigurationFactory factory,
                                         String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    protected ExecuteDQLRunConfigurationOptions getOptions() {
        return (ExecuteDQLRunConfigurationOptions) super.getOptions();
    }

    public String getDQLFile() {
        return getOptions().getDqlPath();
    }

    public void setDQLFile(String dqlFile) {
        getOptions().setDqlPath(dqlFile);
    }

    public String getTenantName() {
        return getOptions().getSelectedTenant();
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new ExecuteDQLSettingsEditor(getProject());
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        FileDocumentManager.getInstance().saveAllDocuments();
        return new ExecuteDQLRunProfileState(this, getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (StringUtil.isEmpty(getDQLFile())) {
            throw new RuntimeConfigurationError(DQLBundle.message("runConfiguration.executeDQL.settings.errors.emptyFile"));
        }
        if (StringUtil.isEmpty(getOptions().getSelectedTenant())) {
            throw new RuntimeConfigurationError(DQLBundle.message("runConfiguration.executeDQL.settings.errors.emptyTenant"));
        }
    }
}

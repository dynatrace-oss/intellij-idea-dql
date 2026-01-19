package pl.thedeem.intellij.dql.exec.runConfiguration;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.components.BaseState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExecuteDQLConfigurationFactory extends ConfigurationFactory {
    public ExecuteDQLConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull String getId() {
        return ExecuteDQLConfigurationType.ID;
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new ExecuteDQLRunConfiguration(project, this, "DQL");
    }

    @Nullable
    @Override
    public Class<? extends BaseState> getOptionsClass() {
        return ExecuteDQLRunConfigurationOptions.class;
    }
}

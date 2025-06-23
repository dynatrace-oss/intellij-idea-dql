package pl.thedeem.intellij.dql.executing.executeDql;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.DQLIcon;

public class ExecuteDQLConfigurationType extends ConfigurationTypeBase {
    public static final String ID = "DQL";

    private static final ExecuteDQLConfigurationType INSTANCE = new ExecuteDQLConfigurationType();

    public static ExecuteDQLConfigurationType getInstance() {
        return INSTANCE;
    }

    protected ExecuteDQLConfigurationType() {
        super(ID, DQLBundle.message("runConfiguration.executeDQL.displayName"),
                DQLBundle.message("runConfiguration.executeDQL.description"),
                DQLIcon.DYNATRACE_LOGO
        );
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new ExecuteDQLConfigurationFactory(this)};
    }

    public ExecuteDQLConfigurationFactory getFactory() {
        return (ExecuteDQLConfigurationFactory) getConfigurationFactories()[0];
    }
}

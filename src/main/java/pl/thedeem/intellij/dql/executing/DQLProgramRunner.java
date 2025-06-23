package pl.thedeem.intellij.dql.executing;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.executing.executeDql.ExecuteDQLRunConfiguration;

public class DQLProgramRunner implements ProgramRunner<RunnerSettings> {
    @NotNull
    @Override
    public String getRunnerId() {
        return "DQLProgramRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return "Run".equals(executorId) && profile instanceof ExecuteDQLRunConfiguration;
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {

        RunProfileState state = environment.getState();
        if (state == null) {
            return;
        }

        Executor executor = environment.getExecutor();

        ExecutionResult result = state.execute(executor, this);
        if (result == null) {
            return;
        }

        final RunContentBuilder contentBuilder = new RunContentBuilder(result, environment);

        if (result.getProcessHandler() != null) {
            result.getProcessHandler().addProcessListener(new ProcessAdapter() {
                @Override
                public void processTerminated(@NotNull ProcessEvent event) {
                    contentBuilder.dispose();
                }
            });
        }
    }

}

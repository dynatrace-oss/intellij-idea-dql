package pl.thedeem.intellij.dql.executing.executeDql.runConfiguration;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;

public class DQLProcessHandler extends ProcessHandler {

    public DQLProcessHandler() {
        addProcessListener(new ProcessListener() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {
                notifyTextAvailable("DQL execution started via API...\n", ProcessOutputTypes.STDOUT);
            }
        });
    }

    @Override
    protected void destroyProcessImpl() {
        notifyTextAvailable("Stopping DQL execution...\n", ProcessOutputTypes.STDOUT);
    }

    @Override
    protected void detachProcessImpl() {
        notifyTextAvailable("Detaching DQL execution...\n", ProcessOutputTypes.STDOUT);
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Nullable
    @Override
    public OutputStream getProcessInput() {
        return null;
    }

    public void notifyExecutionFinished() {
        notifyTextAvailable("DQL execution finished successfully.\n", ProcessOutputTypes.STDOUT);
        notifyProcessTerminated(0);
    }

    public void notifyExecutionError(@Nullable String errorMessage) {
        if (errorMessage != null) {
            notifyTextAvailable("DQL execution failed: " + errorMessage + "\n", ProcessOutputTypes.STDERR);
        } else {
            notifyTextAvailable("DQL execution failed.\n", ProcessOutputTypes.STDERR);
        }
        notifyProcessTerminated(1);
    }
}

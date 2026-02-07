package pl.thedeem.intellij.dql.exec;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessOutputTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.dql.DQLBundle;

import java.io.OutputStream;

public class DQLProcessHandler extends ProcessHandler {

    public DQLProcessHandler() {
        addProcessListener(new ProcessListener() {
            @Override
            public void startNotified(@NotNull ProcessEvent event) {
                notifyTextAvailable(DQLBundle.message("processHandler.execution.started"), ProcessOutputTypes.STDOUT);
            }
        });
    }

    @Override
    protected void destroyProcessImpl() {
        notifyTextAvailable(DQLBundle.message("processHandler.execution.stopping"), ProcessOutputTypes.STDOUT);
    }

    @Override
    protected void detachProcessImpl() {
        notifyTextAvailable(DQLBundle.message("processHandler.execution.detaching"), ProcessOutputTypes.STDOUT);
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
        notifyTextAvailable(DQLBundle.message("processHandler.execution.finished"), ProcessOutputTypes.STDOUT);
        notifyProcessTerminated(0);
    }

    public void notifyExecutionError(@Nullable String errorMessage) {
        if (errorMessage != null) {
            notifyTextAvailable(
                    DQLBundle.message("processHandler.execution.failedWithDetails", errorMessage),
                    ProcessOutputTypes.STDERR
            );
        } else {
            notifyTextAvailable(DQLBundle.message("processHandler.execution.failed"), ProcessOutputTypes.STDERR);
        }
        notifyProcessTerminated(1);
    }
}

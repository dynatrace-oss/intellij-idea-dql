package pl.thedeem.intellij.dql.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.dql.DQLBundle;
import pl.thedeem.intellij.dql.fileProviders.DQLQueryConsoleVirtualFile;

import java.util.concurrent.atomic.AtomicInteger;

public class OpenDQLQueryConsoleAction extends AnAction implements DumbAware {
    public static final AtomicInteger COUNTER = new AtomicInteger(0);
    public static final DataKey<String> INITIAL_TENANT = DataKey.create("DQL.INITIAL_TENANT_FOR_QUERY_CONSOLE");

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        DQLQueryConsoleVirtualFile vf = new DQLQueryConsoleVirtualFile(
                DQLBundle.message("action.DQL.OpenDQLQueryConsole.ServiceViewAction.consoleName", COUNTER.incrementAndGet()),
                e.getData(INITIAL_TENANT)
        );
        FileEditorManager.getInstance(project).openFile(vf, true);
    }
}


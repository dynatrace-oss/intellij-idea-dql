package pl.thedeem.intellij.dql.components.execution;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.sdk.model.DQLPollResponse;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;

public class DQLTableResultPanel extends JPanel {
    public DQLTableResultPanel(@Nullable DQLPollResponse result, @NotNull Project project) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());

        if (result == null) {
            add(new JBScrollPane(new InformationComponent(
                    DQLBundle.message("runConfiguration.executeDQL.infos.emptyRecords"),
                    AllIcons.General.Information
            )), BorderLayout.CENTER);
        } else if (result.getResult() != null && !result.getResult().getRecords().isEmpty()) {
            add(new DQLExecutionTablePanel(project, result.getResult()), BorderLayout.CENTER);
        } else {
            add(new JBScrollPane(new InformationComponent(
                    DQLBundle.message("runConfiguration.executeDQL.infos.emptyRecords"),
                    AllIcons.General.Information
            )), BorderLayout.CENTER);
        }
    }
}

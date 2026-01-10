package pl.thedeem.intellij.dql.components.execution;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.sdk.errors.DQLErrorResponseException;
import pl.thedeem.intellij.common.sdk.errors.DQLNotAuthorizedException;
import pl.thedeem.intellij.common.sdk.errors.DQLResponseParsingException;
import pl.thedeem.intellij.common.sdk.errors.DQLResponseRedirectedException;
import pl.thedeem.intellij.dql.DQLBundle;

import javax.swing.*;
import java.awt.*;

public class DQLExecutionErrorPanel extends JPanel {
    public DQLExecutionErrorPanel(@NotNull Exception e) {
        this(getErrorMessage(e));
    }

    public DQLExecutionErrorPanel(@NotNull String message) {
        setOpaque(false);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());
        JBScrollPane scroll = new JBScrollPane(new InformationComponent(message, AllIcons.General.Error));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);
    }

    private static String getErrorMessage(@NotNull Exception exception) {
        return switch (exception) {
            case DQLErrorResponseException error ->
                    DQLBundle.message("runConfiguration.executeDQL.errors.execution", error.getApiMessage());
            case DQLNotAuthorizedException error ->
                    DQLBundle.message("runConfiguration.executeDQL.errors.unauthorized", error.getApiMessage());
            case DQLResponseParsingException error ->
                    DQLBundle.message("runConfiguration.executeDQL.errors.parsing", error.getApiMessage());
            case DQLResponseRedirectedException error ->
                    DQLBundle.message("runConfiguration.executeDQL.errors.redirected", error.getApiMessage());
            case InterruptedException ignored ->
                    DQLBundle.message("runConfiguration.executeDQL.indicator.cancelled", exception.getMessage());
            default -> DQLBundle.message("runConfiguration.executeDQL.errors.unknown", exception.getMessage());
        };
    }
}

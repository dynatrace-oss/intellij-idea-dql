package pl.thedeem.intellij.dql.components.execution;

import com.intellij.icons.AllIcons;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import pl.thedeem.intellij.common.components.InformationComponent;
import pl.thedeem.intellij.common.sdk.errors.DQLErrorResponseException;
import pl.thedeem.intellij.common.sdk.errors.DQLNotAuthorizedException;
import pl.thedeem.intellij.common.sdk.errors.DQLResponseParsingException;
import pl.thedeem.intellij.common.sdk.errors.DQLResponseRedirectedException;
import pl.thedeem.intellij.common.sdk.model.errors.DQLAuthErrorResponse;
import pl.thedeem.intellij.common.sdk.model.errors.DQLExecutionErrorResponse;
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
        String details = DQLBundle.message("runConfiguration.executeDQL.errors.noDetails");
        return switch (exception) {
            case DQLErrorResponseException error -> {
                if (error.getResponse() != null) {
                    DQLExecutionErrorResponse reason = error.getResponse().error;
                    details = reason.message;
                    if (reason.details.errorMessage instanceof String msg) {
                        details = msg;
                    }
                }
                yield DQLBundle.message("runConfiguration.executeDQL.errors.execution", details);
            }
            case DQLNotAuthorizedException error -> {
                if (error.getResponse() != null) {
                    DQLAuthErrorResponse reason = error.getResponse().error;
                    details = reason.message;
                    if (reason.details.get("errorMessage") instanceof String msg) {
                        details = msg;
                    }
                }
                yield DQLBundle.message("runConfiguration.executeDQL.errors.unauthorized", details);
            }
            case DQLResponseParsingException error -> {
                if (error.getResponse() != null) {
                    details = error.getResponse();
                }
                yield DQLBundle.message("runConfiguration.executeDQL.errors.parsing", details);
            }
            case DQLResponseRedirectedException error -> {
                if (error.getRedirectionUrl() != null) {
                    details = error.getRedirectionUrl();
                }
                yield DQLBundle.message("runConfiguration.executeDQL.errors.redirected", details);
            }
            case InterruptedException ignored ->
                    DQLBundle.message("runConfiguration.executeDQL.indicator.cancelled", exception.getMessage());
            default -> DQLBundle.message("runConfiguration.executeDQL.errors.unknown", exception.getMessage());
        };
    }
}

package pl.thedeem.intellij.dql.services.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface DQLNotificationsService {
    String ERRORS = "DQL.Errors";

    static @NotNull DQLNotificationsService getInstance(@NotNull Project project) {
        return project.getService(DQLNotificationsService.class);
    }

    Notification showNotification(@NotNull String group, @NotNull String title, @NotNull String message, @NotNull NotificationType type, @NotNull Project project);

    Notification showNotification(@NotNull String group, @NotNull String title, @NotNull String message, @NotNull NotificationType type, @NotNull Project project, @NotNull Collection<AnAction> actions);

    Notification showErrorNotification(@NotNull String title, @NotNull String message, @NotNull Project project, @NotNull Collection<AnAction> actions);
}

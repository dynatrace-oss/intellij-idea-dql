package pl.thedeem.intellij.dql.services.notifications;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DQLNotificationsServiceImpl implements DQLNotificationsService {
    private final Map<String, Notification> activeNotifications = new HashMap<>();

    @Override
    public Notification showNotification(@NotNull String group, @NotNull String title, @NotNull String message, @NotNull NotificationType type, @NotNull Project project) {
        return showNotification(group, title, message, type, project, Set.of());
    }

    @Override
    public Notification showNotification(@NotNull String group, @NotNull String title, @NotNull String message, @NotNull NotificationType type, @NotNull Project project, @NotNull Collection<AnAction> actions) {
        Notification notification = activeNotifications.get(title);
        if (notification == null || notification.isExpired()) {
            notification = NotificationGroupManager.getInstance().getNotificationGroup(group).createNotification(title, message, type);
            activeNotifications.put(title, notification);
            notification.setRemoveWhenExpired(true);
            notification.whenExpired(() -> activeNotifications.remove(title));
            if (!actions.isEmpty()) {
                notification.addActions(actions);
            }
            notification.notify(project);
        }
        return notification;
    }

    @Override
    public Notification showErrorNotification(@NotNull String title, @NotNull String message, @NotNull Project project, @NotNull Collection<AnAction> actions) {
        return DQLNotificationsService.getInstance(project).showNotification(
                DQLNotificationsService.ERRORS,
                title,
                message,
                NotificationType.ERROR,
                project,
                actions
        );
    }
}

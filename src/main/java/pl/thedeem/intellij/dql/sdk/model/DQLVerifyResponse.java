package pl.thedeem.intellij.dql.sdk.model;

import java.util.List;

public class DQLVerifyResponse {
    public Boolean valid;
    public List<DQLVerifyNotification> notifications;

    public static final class DQLVerifyNotification {
        public List<Object> arguments;
        public String message;
        public String messageFormat;
        public List<Object> messageFormatSpecifierTypes;
        public String notificationType;
        public String severity;
        public DQLSyntaxErrorPositionDetails syntaxPosition;

        public List<Object> getArguments() {
            return arguments;
        }

        public String getMessage() {
            return message;
        }

        public String getMessageFormat() {
            return messageFormat;
        }

        public List<Object> getMessageFormatSpecifierTypes() {
            return messageFormatSpecifierTypes;
        }

        public String getNotificationType() {
            return notificationType;
        }

        public String getSeverity() {
            return severity;
        }

        public DQLSyntaxErrorPositionDetails getSyntaxPosition() {
            return syntaxPosition;
        }
    }

    public boolean isValid() {
        return Boolean.TRUE.equals(valid);
    }

    public List<DQLVerifyNotification> getNotifications() {
        return notifications != null ? notifications : List.of();
    }
}

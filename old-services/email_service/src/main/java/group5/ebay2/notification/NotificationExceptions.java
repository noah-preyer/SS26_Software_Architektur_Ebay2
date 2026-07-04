package group5.ebay2.notification;

public class NotificationExceptions {

    public static class EmailTemplateNotFoundException extends RuntimeException {
        public EmailTemplateNotFoundException(String message) {
            super(message);
        }
    }

    public static class EmailNotificationNotFoundException extends RuntimeException {
        public EmailNotificationNotFoundException(String message) {
            super(message);
        }
    }
}

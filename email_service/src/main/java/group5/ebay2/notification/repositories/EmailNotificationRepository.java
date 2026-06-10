package group5.ebay2.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import group5.ebay2.notification.EmailNotification;

import java.util.UUID;

public interface EmailNotificationRepository extends JpaRepository<EmailNotification, UUID> {
}

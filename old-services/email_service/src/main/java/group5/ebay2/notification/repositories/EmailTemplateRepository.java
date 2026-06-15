package group5.ebay2.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import group5.ebay2.notification.EmailTemplate;

import java.util.Optional;
import java.util.UUID;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {

    Optional<EmailTemplate> findByCodeAndActiveTrue(String code);
}

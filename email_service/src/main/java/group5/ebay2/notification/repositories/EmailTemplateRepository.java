package group5.ebay2.notification.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import group5.ebay2.notification.EmailTemplate;

import java.util.Optional;

public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByCodeAndActiveTrue(String code);
}

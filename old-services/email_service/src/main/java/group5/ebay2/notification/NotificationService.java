package group5.ebay2.notification;

import group5.ebay2.notification.dtos.EmailDto;
import group5.ebay2.notification.repositories.EmailNotificationRepository;
import group5.ebay2.notification.repositories.EmailTemplateRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final EmailNotificationRepository emailNotificationRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public NotificationService(EmailNotificationRepository emailNotificationRepository,
                               EmailTemplateRepository emailTemplateRepository,
                               JavaMailSender mailSender,
                               @Value("${spring.mail.from}") String fromAddress) {
        this.emailNotificationRepository = emailNotificationRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Transactional
    public EmailDto.Response sendEmail(EmailDto.SendRequest request) {
        log.info("Sending email to: {} using template: {}", request.recipientEmail(), request.templateCode());

        EmailTemplate template = emailTemplateRepository.findByCodeAndActiveTrue(request.templateCode())
                .orElseThrow(() -> new NotificationExceptions.EmailTemplateNotFoundException(
                        "Template not found or inactive: " + request.templateCode()));

        String subject = resolvePlaceholders(template.getSubjectTemplate(), request.placeholders());
        String body = resolvePlaceholders(template.getBodyTemplate(), request.placeholders());

        EmailNotification notification = new EmailNotification(
                request.recipientEmail(), subject, body, template
        );

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(request.recipientEmail());
            helper.setSubject(subject);
            helper.setText(body, false);

            mailSender.send(message);

            notification.markSent();
            log.info("Successfully sent email to: {}", request.recipientEmail());
        } catch (Exception e) {
            notification.markFailed(e.getMessage());
            log.error("Failed to send email to: {}: {}", request.recipientEmail(), e.getMessage());
        }

        EmailNotification saved = emailNotificationRepository.save(notification);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmailDto.Response getNotification(UUID id) {
        return toResponse(emailNotificationRepository.findById(id)
                .orElseThrow(() -> new NotificationExceptions.EmailNotificationNotFoundException(
                        "Email notification not found: " + id)));
    }

    private String resolvePlaceholders(String template, Map<String, String> placeholders) {
        if (placeholders == null) return template;
        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return result.replace("\\n", "\n");
    }

    private EmailDto.Response toResponse(EmailNotification notification) {
        return new EmailDto.Response(
                notification.getId(),
                notification.getRecipientEmail(),
                notification.getSubject(),
                notification.getBody(),
                notification.getStatus(),
                notification.getSentAt(),
                notification.getTemplate() != null ? notification.getTemplate().getCode() : null
        );
    }
}

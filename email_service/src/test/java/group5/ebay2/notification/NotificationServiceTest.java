package group5.ebay2.notification;

import group5.ebay2.notification.dtos.EmailDto;
import group5.ebay2.notification.repositories.EmailNotificationRepository;
import group5.ebay2.notification.repositories.EmailTemplateRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailNotificationRepository emailNotificationRepository;

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        reset(mailSender);
        emailNotificationRepository.deleteAll();
        emailNotificationRepository.flush();
        emailTemplateRepository.deleteAll();
        emailTemplateRepository.flush();
        emailTemplateRepository.save(new EmailTemplate(
                "ORDER_CONFIRMATION", "Order Confirmation",
                "Order #${orderId} confirmed",
                "Hi ${username}, your order #${orderId} is confirmed. Total: ${orderTotal}"
        ));

        when(mailSender.createMimeMessage()).thenAnswer(i ->
                new MimeMessage(Session.getDefaultInstance(new Properties())));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        JavaMailSender mailSender() {
            return mock(JavaMailSender.class);
        }
    }

    @Test
    void sendEmail_shouldResolvePlaceholdersAndSendMail() {
        EmailDto.Response response = notificationService.sendEmail(new EmailDto.SendRequest(
                "alice@example.com", "ORDER_CONFIRMATION",
                Map.of("orderId", "12345", "username", "alice", "orderTotal", "$49.99")
        ));

        assertThat(response.id()).isNotNull();
        assertThat(response.recipientEmail()).isEqualTo("alice@example.com");
        assertThat(response.subject()).isEqualTo("Order #12345 confirmed");
        assertThat(response.body()).contains("Hi alice").contains("$49.99");
        assertThat(response.status()).isEqualTo("SENT");
        assertThat(response.sentAt()).isNotNull();
        assertThat(response.templateCode()).isEqualTo("ORDER_CONFIRMATION");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_shouldThrowOnMissingTemplate() {
        assertThatThrownBy(() -> notificationService.sendEmail(new EmailDto.SendRequest(
                "bob@example.com", "FAKE_CODE", Map.of())))
                .isInstanceOf(NotificationExceptions.EmailTemplateNotFoundException.class);
    }

    @Test
    void getNotification_shouldReturnSavedNotification() {
        EmailDto.Response sent = notificationService.sendEmail(new EmailDto.SendRequest(
                "bob@example.com", "ORDER_CONFIRMATION", Map.of("orderId", "1", "username", "bob", "orderTotal", "$10")
        ));

        EmailDto.Response found = notificationService.getNotification(sent.id());

        assertThat(found.id()).isEqualTo(sent.id());
        assertThat(found.recipientEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void getNotification_shouldThrowOnNotFound() {
        assertThatThrownBy(() -> notificationService.getNotification(UUID.randomUUID()))
                .isInstanceOf(NotificationExceptions.EmailNotificationNotFoundException.class);
    }
}

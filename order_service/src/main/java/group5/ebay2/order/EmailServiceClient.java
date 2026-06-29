package group5.ebay2.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class EmailServiceClient {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceClient.class);

    private final RestTemplate restTemplate;
    private final String emailServiceUrl;
    private final UserServiceClient userServiceClient;

    public EmailServiceClient(RestTemplate restTemplate, UserServiceClient userServiceClient) {
        this.restTemplate = restTemplate;
        this.emailServiceUrl = System.getenv().getOrDefault("EMAIL_SERVICE_URL", "http://notification-service:8080");
        this.userServiceClient = userServiceClient;
    }

    public void sendPaymentConfirmation(Long userId, String orderId, String productTitle, String amount, String currency) {
        try {
            UserDto user = userServiceClient.getUser(userId);
            if (user == null || user.email() == null) {
                log.warn("Could not find user or email for user {}", userId);
                return;
            }

            Map<String, String> placeholders = Map.of(
                    "orderId", orderId,
                    "productTitle", productTitle,
                    "amount", amount,
                    "currency", currency
            );

            Map<String, Object> request = Map.of(
                    "recipientEmail", user.email(),
                    "templateCode", "ORDER_CONFIRMATION",
                    "placeholders", placeholders
            );

            restTemplate.postForObject(emailServiceUrl + "/notification/send", request, Object.class);
            log.info("Payment confirmation email sent to {} for order {}", user.email(), orderId);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email for order {}: {}", orderId, e.getMessage());
        }
    }

    public void sendShippingConfirmation(Long userId, String orderId, String productTitle) {
        try {
            UserDto user = userServiceClient.getUser(userId);
            if (user == null || user.email() == null) {
                log.warn("Could not find user or email for user {}", userId);
                return;
            }

            Map<String, String> placeholders = Map.of(
                    "orderId", orderId,
                    "productTitle", productTitle
            );

            Map<String, Object> request = Map.of(
                    "recipientEmail", user.email(),
                    "templateCode", "SHIPPING_CONFIRMATION",
                    "placeholders", placeholders
            );

            restTemplate.postForObject(emailServiceUrl + "/notification/send", request, Object.class);
            log.info("Shipping confirmation email sent to {} for order {}", user.email(), orderId);
        } catch (Exception e) {
            log.error("Failed to send shipping confirmation email for order {}: {}", orderId, e.getMessage());
        }
    }

    public void sendDeliveryConfirmation(Long userId, String orderId, String productTitle) {
        try {
            UserDto user = userServiceClient.getUser(userId);
            if (user == null || user.email() == null) {
                log.warn("Could not find user or email for user {}", userId);
                return;
            }

            Map<String, String> placeholders = Map.of(
                    "orderId", orderId,
                    "productTitle", productTitle
            );

            Map<String, Object> request = Map.of(
                    "recipientEmail", user.email(),
                    "templateCode", "DELIVERY_CONFIRMATION",
                    "placeholders", placeholders
            );

            restTemplate.postForObject(emailServiceUrl + "/notification/send", request, Object.class);
            log.info("Delivery confirmation email sent to {} for order {}", user.email(), orderId);
        } catch (Exception e) {
            log.error("Failed to send delivery confirmation email for order {}: {}", orderId, e.getMessage());
        }
    }

    public void sendRefundConfirmation(Long userId, String orderId, String productTitle, String amount, String currency) {
        try {
            UserDto user = userServiceClient.getUser(userId);
            if (user == null || user.email() == null) {
                log.warn("Could not find user or email for user {}", userId);
                return;
            }

            Map<String, String> placeholders = Map.of(
                    "orderId", orderId,
                    "productTitle", productTitle,
                    "amount", amount,
                    "currency", currency
            );

            Map<String, Object> request = Map.of(
                    "recipientEmail", user.email(),
                    "templateCode", "ORDER_CONFIRMATION",
                    "placeholders", placeholders
            );

            restTemplate.postForObject(emailServiceUrl + "/notification/send", request, Object.class);
            log.info("Refund confirmation email sent to {} for order {}", user.email(), orderId);
        } catch (Exception e) {
            log.error("Failed to send refund confirmation email for order {}: {}", orderId, e.getMessage());
        }
    }
}

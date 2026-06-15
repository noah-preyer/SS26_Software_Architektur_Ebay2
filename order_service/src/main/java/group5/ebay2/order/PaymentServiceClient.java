package group5.ebay2.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class PaymentServiceClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceClient.class);

    private final RestTemplate restTemplate;
    private final String paymentServiceUrl;

    public PaymentServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.paymentServiceUrl = System.getenv().getOrDefault("PAYMENT_SERVICE_URL", "http://payment-service:8080");
    }

    public void refundPayment(UUID orderId) {
        try {
            restTemplate.put(paymentServiceUrl + "/payment/order/" + orderId + "/refund", null);
            log.info("Refund processed for order: {}", orderId);
        } catch (Exception e) {
            log.error("Failed to process refund for order {}: {}", orderId, e.getMessage());
            throw e;
        }
    }
}

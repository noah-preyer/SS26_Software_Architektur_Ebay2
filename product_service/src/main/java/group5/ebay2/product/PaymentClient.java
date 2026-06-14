package group5.ebay2.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClient(@Value("${services.payment.url}") String paymentUrl) {
        this.restClient = RestClient.builder().baseUrl(paymentUrl).build();
    }

    public OrderResponse createOrder(UUID userId, UUID productId, BigDecimal amount, String currency) {
        return restClient.post()
                .uri("/order")
                .body(new CreateOrderRequest(userId, productId, amount, currency))
                .retrieve()
                .body(OrderResponse.class);
    }

    public PaymentResponse processPayment(UUID orderId, UUID productId, UUID userId,
                                          BigDecimal amount, String currency, String paymentMethodType) {
        return restClient.post()
                .uri("/payment/process")
                .body(new ProcessPaymentRequest(orderId, productId.toString(), userId, amount, currency, paymentMethodType))
                .retrieve()
                .body(PaymentResponse.class);
    }

    record CreateOrderRequest(UUID userId, UUID productId, BigDecimal totalAmount, String currency) {}

    record ProcessPaymentRequest(UUID orderId, String productId, UUID userId,
                                 BigDecimal amount, String currency, String paymentMethodType) {}

    public record OrderResponse(UUID id, UUID userId, UUID productId, String status,
                                BigDecimal totalAmount, String currency) {}

    public record PaymentResponse(UUID id, UUID orderId, String productId, UUID userId,
                                  BigDecimal amount, String currency, String status,
                                  String transactionId, Instant paidAt) {}
}

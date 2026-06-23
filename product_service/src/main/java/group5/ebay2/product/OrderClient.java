package group5.ebay2.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${services.order.url}") String orderUrl) {
        this.restClient = RestClient.builder().baseUrl(orderUrl).build();
    }

    public OrderResponse createOrder(Long userId, Long productId, String currency) {
        return restClient.post()
                .uri("/order")
                .body(new CreateOrderRequest(userId, productId, currency))
                .retrieve()
                .body(OrderResponse.class);
    }

    public OrderResponse markOrderPaid(UUID orderId) {
        return restClient.put()
                .uri("/order/{id}/paid", orderId)
                .retrieve()
                .body(OrderResponse.class);
    }

    record CreateOrderRequest(Long userId, Long productId, String currency) {}

    public record OrderResponse(UUID id, Long userId, Long productId, String status,
                                BigDecimal totalAmount, String currency) {}
}
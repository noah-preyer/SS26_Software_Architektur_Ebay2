package group5.ebay2.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${services.order.url}") String orderUrl) {
        this.restClient = RestClient.builder().baseUrl(orderUrl).build();
    }

    public OrderResponse createOrder(Long userId, Long productId, String currency) {
        return createBulkOrder(userId, List.of(new CreateOrderItem(productId, 1)), currency);
    }

    public OrderResponse createBulkOrder(Long userId, List<CreateOrderItem> items, String currency) {
        return restClient.post()
                .uri("/")
                .body(new CreateOrderRequest(userId, items, currency))
                .retrieve()
                .body(OrderResponse.class);
    }

    public OrderResponse markOrderPaid(Long orderId) {
        return restClient.put()
                .uri("/{id}/paid", orderId)
                .retrieve()
                .body(OrderResponse.class);
    }

    record CreateOrderRequest(Long userId, List<CreateOrderItem> items, String currency) {}
    record CreateOrderItem(Long productId, Integer quantity) {}

    public record OrderResponse(Long id, Long userId, String status,
                                BigDecimal totalAmount, String currency) {}
}
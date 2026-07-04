package group5.ebay2.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class OrderServiceClient {

    private final RestClient restClient;

    public OrderServiceClient(@Value("${services.order.url}") String orderUrl) {
        this.restClient = RestClient.builder().baseUrl(orderUrl).build();
    }

    public OrderResponse createOrder(Long userId, Long productId, String currency) {
        return restClient.post()
                .uri("/order")
                .body(new CreateOrderRequest(userId, productId, currency))
                .retrieve()
                .body(OrderResponse.class);
    }

    record CreateOrderRequest(Long userId, Long productId, String currency) {}

    public record OrderResponse(Long id, Long userId, Long productId, String status,
                                BigDecimal totalAmount, String currency) {
    }
}

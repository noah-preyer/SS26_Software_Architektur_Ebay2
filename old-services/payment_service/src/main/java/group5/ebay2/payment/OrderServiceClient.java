package group5.ebay2.payment;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    private final String orderServiceUrl;

    public OrderServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.orderServiceUrl = System.getenv().getOrDefault("ORDER_SERVICE_URL", "http://order-service:8080");
    }

    public OrderDto getOrder(java.util.UUID orderId) {
        return restTemplate.getForObject(orderServiceUrl + "/order/" + orderId, OrderDto.class);
    }

    public void markOrderPaid(java.util.UUID orderId) {
        restTemplate.put(orderServiceUrl + "/order/" + orderId + "/paid", null);
    }

    public void markOrderRefunded(java.util.UUID orderId) {
        restTemplate.put(orderServiceUrl + "/order/" + orderId + "/refunded", null);
    }
}

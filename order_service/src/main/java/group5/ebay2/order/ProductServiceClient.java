package group5.ebay2.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class ProductServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceClient.class);

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = System.getenv().getOrDefault("PRODUCT_SERVICE_URL", "http://product-service:8082");
    }

    public ProductDto getProduct(UUID productId) {
        try {
            return restTemplate.getForObject(productServiceUrl + "/products/" + productId, ProductDto.class);
        } catch (Exception e) {
            log.error("Failed to fetch product {}: {}", productId, e.getMessage());
            return null;
        }
    }
}
